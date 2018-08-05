package josecponce.springbatchhelpers;

import josecponce.springbatchhelpers.readers.ItemGenerator;
import josecponce.springbatchhelpers.stepbuilder.ParallelJpaToJpaStepBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {ItemGenerator.class, ParallelJpaToJpaStepBuilder.class})
public class SpringBatchHelpersConfiguration {
}
