package cl.recetaya.dispensacion.configuracion;

import cl.recetaya.dispensacion.integracion.ValidadorRecetas;
import cl.recetaya.dispensacion.integracion.ValidadorRecetasNoDisponible;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionValidadorRecetas {
    @Bean
    @ConditionalOnMissingBean(ValidadorRecetas.class)
    ValidadorRecetas validadorRecetasNoDisponible() {
        return new ValidadorRecetasNoDisponible();
    }
}
