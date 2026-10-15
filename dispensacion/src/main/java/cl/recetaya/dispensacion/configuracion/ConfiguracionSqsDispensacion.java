package cl.recetaya.dispensacion.configuracion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "recetaya.integracion.inventario.sqs-habilitada", havingValue = "true")
public class ConfiguracionSqsDispensacion {
    @Bean(destroyMethod = "close")
    SqsClient clienteSqsDispensacion(Environment entorno) {
        exigirCola(entorno, "RECETAYA_SQS_SOLICITUDES");
        exigirCola(entorno, "RECETAYA_SQS_RESULTADOS_DISPENSACION");
        String region = entorno.getProperty("AWS_REGION", "us-east-1");
        return SqsClient.builder().region(Region.of(region)).build();
    }

    private void exigirCola(Environment entorno, String nombre) {
        if (entorno.getProperty(nombre, "").isBlank()) {
            throw new IllegalStateException("Debe configurar " + nombre + " al habilitar SQS.");
        }
    }
}
