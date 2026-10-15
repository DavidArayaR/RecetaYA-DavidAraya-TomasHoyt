package cl.recetaya.dispensacion.configuracion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "recetaya.integracion.inventario.sqs-habilitada", havingValue = "true")
public class ConfiguracionSqsDispensacion {
    @Bean(destroyMethod = "close")
    SqsClient clienteSqsDispensacion(org.springframework.core.env.Environment entorno) {
        String region = entorno.getProperty("AWS_REGION", "us-east-1");
        return SqsClient.builder().region(Region.of(region)).build();
    }
}
