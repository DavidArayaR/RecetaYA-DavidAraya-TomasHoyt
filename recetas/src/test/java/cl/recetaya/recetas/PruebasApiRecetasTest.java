package cl.recetaya.recetas;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:recetas_pruebas;DB_CLOSE_DELAY=-1",
        "recetaya.seguridad.jwt.secreto=solo-pruebas-no-usar-en-produccion-0123456789"
})
class PruebasApiRecetasTest {
    private static final String RUTA = "/api/v1/recetas";
    private static final UUID MEDICO = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID OTRO_MEDICO = UUID.fromString("00000000-0000-0000-0000-000000000222");
    private static final UUID SUCURSAL = UUID.fromString("00000000-0000-0000-0000-000000000333");
    private static final UUID MEDICAMENTO = UUID.fromString("00000000-0000-0000-0000-000000000444");

    @Autowired
    MockMvc mvc;

    private org.springframework.test.web.servlet.request.RequestPostProcessor medico(UUID id) {
        return jwt().jwt(token -> token.subject(id.toString()).claim("roles", List.of("MEDICO")))
                .authorities(new SimpleGrantedAuthority("ROLE_MEDICO"));
    }

    private String solicitud(String documento, int cantidad) {
        return """
                {
                  "paciente": {"documento":"%s", "nombre":"Ana Pérez"},
                  "sucursalId": "%s",
                  "medicamentos": [{"medicamentoId":"%s", "cantidad": %d}]
                }
                """.formatted(documento, SUCURSAL, MEDICAMENTO, cantidad);
    }

    private MvcResult crear(String documento) throws Exception {
        return mvc.perform(post(RUTA).with(medico(MEDICO)).contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(documento, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACEPTADA_PENDIENTE_RESERVA"))
                .andExpect(jsonPath("$.medicoUsuarioId").value(MEDICO.toString()))
                .andExpect(jsonPath("$.medicamentos[0].cantidad").value(2))
                .andReturn();
    }

    private String idCreado(MvcResult resultado) throws Exception {
        String json = resultado.getResponse().getContentAsString();
        return json.split("\"id\":\"")[1].split("\"")[0];
    }

    @Test
    void crearConsultarYListarSoloPropias() throws Exception {
        String id = idCreado(crear("PAC-101"));
        mvc.perform(get(RUTA + "/" + id).with(medico(MEDICO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get(RUTA).with(medico(MEDICO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get(RUTA + "/" + id).with(medico(OTRO_MEDICO)))
                .andExpect(status().isNotFound());
        mvc.perform(get(RUTA).with(medico(OTRO_MEDICO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void actualizacionSePersisteYAnulacionBloqueaCambios() throws Exception {
        String id = idCreado(crear("PAC-102"));
        mvc.perform(put(RUTA + "/" + id).with(medico(MEDICO))
                        .contentType(MediaType.APPLICATION_JSON).content(solicitud("PAC-102", 5)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.medicamentos[0].cantidad").value(5));
        mvc.perform(get(RUTA + "/" + id).with(medico(MEDICO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.medicamentos[0].cantidad").value(5));
        mvc.perform(post(RUTA + "/" + id + "/anular").with(medico(MEDICO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ANULADA"));
        mvc.perform(put(RUTA + "/" + id).with(medico(MEDICO))
                        .contentType(MediaType.APPLICATION_JSON).content(solicitud("PAC-102", 3)))
                .andExpect(status().isConflict());
        mvc.perform(post(RUTA + "/" + id + "/anular").with(medico(MEDICO)))
                .andExpect(status().isConflict());
    }

    @Test
    void validaCamposInvalidosMedicamentosRepetidosYPacienteExistente() throws Exception {
        mvc.perform(post(RUTA).with(medico(MEDICO)).contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("PAC-103", -3)))
                .andExpect(status().isBadRequest());
        mvc.perform(post(RUTA).with(medico(MEDICO)).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        String duplicados = """
                {"paciente":{"documento":"PAC-103","nombre":"Ana Pérez"},
                "sucursalId":"%s",
                "medicamentos":[{"medicamentoId":"%s","cantidad":2},
                {"medicamentoId":"%s","cantidad":1}]}
                """.formatted(SUCURSAL, MEDICAMENTO, MEDICAMENTO);
        mvc.perform(post(RUTA).with(medico(MEDICO)).contentType(MediaType.APPLICATION_JSON)
                        .content(duplicados))
                .andExpect(status().isConflict());
        crear("PAC-103");
        mvc.perform(post(RUTA).with(medico(MEDICO)).contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("PAC-103", 1).replace("Ana Pérez", "Persona Distinta")))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarEliminaMedicamentoAnteriorYAgregaUnoNuevo() throws Exception {
        String id = idCreado(crear("PAC-105"));
        UUID otroMedicamento = UUID.fromString("00000000-0000-0000-0000-000000000555");
        String cambio = solicitud("PAC-105", 4).replace(MEDICAMENTO.toString(), otroMedicamento.toString());
        mvc.perform(put(RUTA + "/" + id).with(medico(MEDICO))
                        .contentType(MediaType.APPLICATION_JSON).content(cambio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamentos.length()").value(1))
                .andExpect(jsonPath("$.medicamentos[0].medicamentoId").value(otroMedicamento.toString()));
        mvc.perform(get(RUTA + "/" + id).with(medico(MEDICO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamentos.length()").value(1))
                .andExpect(jsonPath("$.medicamentos[0].medicamentoId").value(otroMedicamento.toString()));
    }

    @Test
    void restringeAccesoSinTokenYConRolInsuficiente() throws Exception {
        mvc.perform(get(RUTA)).andExpect(status().isUnauthorized());
        mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("PAC-104", 2)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(RUTA)
                        .with(jwt().jwt(token -> token.subject(MEDICO.toString()).claim("roles", List.of("FARMACEUTICO")))
                                .authorities(new SimpleGrantedAuthority("ROLE_FARMACEUTICO")))
                        .contentType(MediaType.APPLICATION_JSON).content(solicitud("PAC-104", 2)))
                .andExpect(status().isForbidden());
    }
}
