package cl.recetaya.recetas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:recetas_contexto;DB_CLOSE_DELAY=-1")
class PruebasAplicacionRecetas {
    @Test
    void cargaElContexto() { }
}
