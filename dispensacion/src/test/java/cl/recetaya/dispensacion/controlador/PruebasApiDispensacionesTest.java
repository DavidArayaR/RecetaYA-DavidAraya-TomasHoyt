package cl.recetaya.dispensacion.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.recetaya.dispensacion.integracion.DetalleRecetaValidada;
import cl.recetaya.dispensacion.integracion.RecetaDispensable;
import cl.recetaya.dispensacion.integracion.ValidadorRecetas;
import cl.recetaya.dispensacion.repositorio.RepositorioDispensacion;
import cl.recetaya.dispensacion.repositorio.RepositorioEventoInventarioProcesado;
import cl.recetaya.dispensacion.repositorio.RepositorioSolicitudConsumo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PruebasApiDispensacionesTest {
    @Autowired MockMvc solicitudesHttp;
    @Autowired RepositorioDispensacion repositorioDispensaciones;
    @Autowired RepositorioSolicitudConsumo repositorioSolicitudes;
    @Autowired RepositorioEventoInventarioProcesado repositorioEventos;
    @MockitoBean ValidadorRecetas validadorRecetas;

    @BeforeEach
    void limpiarBase() {
        repositorioEventos.deleteAll();
        repositorioSolicitudes.deleteAll();
        repositorioDispensaciones.deleteAll();
    }

    @Test
    void registraConEstadoPendienteYConservaOutboxEnH2() throws Exception {
        UUID recetaId = UUID.randomUUID();
        UUID reservaId = UUID.randomUUID();
        UUID medicamentoId = UUID.randomUUID();
        UUID farmaceuticoId = UUID.randomUUID();
        when(validadorRecetas.consultarRecetaDispensable(recetaId)).thenReturn(
                new RecetaDispensable(recetaId, reservaId, UUID.randomUUID(), "RESERVADA",
                        List.of(new DetalleRecetaValidada(medicamentoId, 2))));

        solicitudesHttp.perform(post("/api/v1/dispensaciones")
                        .with(jwt().jwt(token -> token.subject(farmaceuticoId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_FARMACEUTICO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recetaId\":\"%s\"}".formatted(recetaId)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_CONSUMO"))
                .andExpect(jsonPath("$.medicamentos[0].cantidad").value(2));

        org.junit.jupiter.api.Assertions.assertEquals(1, repositorioDispensaciones.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, repositorioSolicitudes.count());

        solicitudesHttp.perform(post("/api/v1/dispensaciones")
                        .with(jwt().jwt(token -> token.subject(farmaceuticoId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_FARMACEUTICO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recetaId\":\"%s\"}".formatted(recetaId)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_CONSUMO"));
        org.junit.jupiter.api.Assertions.assertEquals(1, repositorioDispensaciones.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, repositorioSolicitudes.count());
    }

    @Test
    void exigeJwtYRolFarmaceutico() throws Exception {
        solicitudesHttp.perform(get("/api/v1/dispensaciones"))
                .andExpect(status().isUnauthorized());
        solicitudesHttp.perform(get("/api/v1/dispensaciones")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MEDICO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void noCreaDispensacionSiRecetasNoTieneContratoDisponible() throws Exception {
        UUID recetaId = UUID.randomUUID();
        when(validadorRecetas.consultarRecetaDispensable(any()))
                .thenThrow(new cl.recetaya.dispensacion.excepcion.IntegracionNoDisponibleException(
                        "Integración de recetas no configurada."));
        solicitudesHttp.perform(post("/api/v1/dispensaciones")
                        .with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_FARMACEUTICO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recetaId\":\"%s\"}".formatted(recetaId)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("Integración de recetas no configurada."));
        org.junit.jupiter.api.Assertions.assertEquals(0, repositorioDispensaciones.count());
    }
}
