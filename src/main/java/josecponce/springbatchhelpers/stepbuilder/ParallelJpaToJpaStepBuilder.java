package josecponce.springbatchhelpers.stepbuilder;

import josecponce.springbatchhelpers.CompositeList;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Component
@AllArgsConstructor
@Scope("prototype")
@Slf4j
public class ParallelJpaToJpaStepBuilder<In, Out> {
    @Wither private String name;
    /**
     * This can never be set as a lambda, it has to be set as an annonymous inner class
     * because lambdas lose their type parameters to type erasure at compile time.
     */
    @Wither private ItemProcessor<In, Out> processor;
    @Wither private ItemReader<In> reader;

    @Wither private int chunk = 100;
    @Wither private int concurrency = 1;
    @Wither private String orderBy = "";
    @Wither private Class<Out> outClass;

    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor executor;
    private final StepBuilderFactory stepBuilder;
    private final JpaItemWriter jpaWriter;

    @Autowired
    public ParallelJpaToJpaStepBuilder(EntityManagerFactory entityManagerFactory,
                                       PlatformTransactionManager transactionManager,
                                       TaskExecutor executor, StepBuilderFactory stepBuilder, JpaItemWriter jpaWriter) {
        this.entityManagerFactory = entityManagerFactory;
        this.transactionManager = transactionManager;
        this.executor = executor;
        this.stepBuilder = stepBuilder;
        this.jpaWriter = jpaWriter;
    }

    @SuppressWarnings("unchecked")
    public Step build() {
        Class[] types = outClass != null ? new Class[] {null, outClass} :
                GenericTypeResolver.resolveTypeArguments(processor.getClass(), ItemProcessor.class);
        if (types == null) {
            throw new RuntimeException("The processor needs to be set as an anonymous inner class.");
        }

        ItemWriter writer = !List.class.isAssignableFrom(types[1]) ? jpaWriter : items -> jpaWriter.write(new CompositeList(items));

        SimpleStepBuilder intermediateBuilder = stepBuilder.get(name)
                .transactionManager(transactionManager)
                .chunk(chunk)
                .faultTolerant().skipPolicy(new AlwaysSkipItemSkipPolicy())
                .retry(LockAcquisitionException.class).retryLimit(5)
                .processorNonTransactional()
                .reader(reader != null ? reader :
                        new JpaPagingItemReaderBuilder<In>().name(name + "Reader")
                                .entityManagerFactory(entityManagerFactory).saveState(false).pageSize(chunk)
                                .queryString(String.format("SELECT item FROM %s item %s", types[0].getSimpleName(),
                                        StringUtils.isEmpty(orderBy) ? "" : "ORDER BY " + orderBy)).build())
                .listener(new ErrorLoggingItemProcessListener())
                .writer(writer);
        if (processor != null) {
            intermediateBuilder = intermediateBuilder.processor(processor);
        }
        return intermediateBuilder.taskExecutor(executor).throttleLimit(concurrency).build();
    }
}
