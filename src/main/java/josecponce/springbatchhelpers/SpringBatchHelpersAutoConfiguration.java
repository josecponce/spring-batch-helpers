package josecponce.springbatchhelpers;

import josecponce.springbatchhelpers.readers.ItemGenerator;
import josecponce.springbatchhelpers.stepbuilder.ParallelJpaToJpaStepBuilder;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManagerFactory;

@Configuration
@ComponentScan(basePackageClasses = {ItemGenerator.class, ParallelJpaToJpaStepBuilder.class})
public class SpringBatchHelpersAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setDaemon(true);
        executor.setMaxPoolSize(30);
        executor.setCorePoolSize(10);
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaItemWriter jpaWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter jpaWriter = new JpaItemWriter<>();
        jpaWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaWriter;
    }
}
