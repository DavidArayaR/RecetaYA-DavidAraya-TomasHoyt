package cl.recetaya.autenticacion;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.recetaya.autenticacion.entidad.Usuario;
import cl.recetaya.autenticacion.repositorio.RepositorioUsuario;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PruebasIntegracionAutenticacionTest {
    private static final String CONTRASENA = "ClaveSegura123!";

    @Autowired
    private MockMvc solicitudes;

    @Autowired
    private RepositorioUsuario repositorioUsuarios;

    @Autowired
    private PasswordEncoder codificadorContrasenas;

    @Autowired
    private JwtEncoder codificadorJwt;

    @Autowired
    private SecretKey claveJwt;

    @Test
    void registraUsuarioConHashYRolMedicoSinExponerElHash() throws Exception {
        String correo = correoUnico();

        solicitudes.perform(post("/api/v1/autenticacion/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoElectronico":"%s","contrasena":"%s","nombre":"Médico de prueba"}
                                """.formatted(correo, CONTRASENA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenAcceso").isNotEmpty())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.usuario.correoElectronico").value(correo))
                .andExpect(jsonPath("$.usuario.roles", contains("MEDICO")))
                .andExpect(jsonPath("$.usuario.hashContrasena").doesNotExist());

        Usuario usuario = repositorioUsuarios.findByCorreoElectronicoIgnoreCase(correo).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals(CONTRASENA, usuario.getPassword());
        org.junit.jupiter.api.Assertions.assertTrue(codificadorContrasenas.matches(CONTRASENA, usuario.getPassword()));
    }

    @Test
    void rechazaRegistroDuplicadoConConflicto() throws Exception {
        String correo = correoUnico();
        String cuerpo = """
                {"correoElectronico":"%s","contrasena":"%s","nombre":"Médico de prueba"}
                """.formatted(correo, CONTRASENA);

        solicitudes.perform(post("/api/v1/autenticacion/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        solicitudes.perform(post("/api/v1/autenticacion/registro").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void iniciaSesionPorRutaActualYConservaAliasAnterior() throws Exception {
        String correo = registrarUsuario();
        String cuerpo = """
                {"correoElectronico":"%s","contrasena":"%s"}
                """.formatted(correo, CONTRASENA);

        solicitudes.perform(post("/api/v1/autenticacion/iniciar-sesion")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenAcceso").isNotEmpty());
        solicitudes.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenAcceso").isNotEmpty());
    }

    @Test
    void credencialesIncorrectasYUsuarioInexistenteDevuelvenMismoEstadoYMensaje() throws Exception {
        String correo = registrarUsuario();
        String incorrecta = autenticar(correo, "incorrecta");
        String inexistente = autenticar(correoUnico(), CONTRASENA);

        org.junit.jupiter.api.Assertions.assertEquals(incorrecta, inexistente);
    }

    @Test
    void solicitudesInvalidasDevuelvenBadRequest() throws Exception {
        solicitudes.perform(post("/api/v1/autenticacion/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"no-es-correo\",\"contrasena\":\"corta\",\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaContrasenaQueExcedeElLimiteSeguroDeBytesDeBcrypt() throws Exception {
        solicitudes.perform(post("/api/v1/autenticacion/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoElectronico":"%s","contrasena":"%s","nombre":"Médico de prueba"}
                                """.formatted(correoUnico(), "á".repeat(37))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jsonMalformadoDevuelveErrorControlado() throws Exception {
        solicitudes.perform(post("/api/v1/autenticacion/iniciar-sesion")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"correoElectronico\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("JSON inválido"));
    }

    @Test
    void tokenValidoPermitePerfilYExponeSoloDatosDeUsuario() throws Exception {
        String correo = registrarUsuario();
        String token = iniciarSesion(correo);

        solicitudes.perform(get("/api/v1/autenticacion/perfil").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correoElectronico").value(correo))
                .andExpect(jsonPath("$.roles", contains("MEDICO")))
                .andExpect(jsonPath("$.hashContrasena").doesNotExist());
    }

    @Test
    void perfilRechazaTokenAusenteAlteradoExpiradoYConEmisorIncorrecto() throws Exception {
        String correo = registrarUsuario();
        String token = iniciarSesion(correo);
        String[] partes = token.split("\\.");
        String firmaAlterada = partes[2].charAt(0) == 'a' ? "b" : "a";
        String alterado = partes[0] + "." + partes[1] + "." + firmaAlterada + partes[2].substring(1);

        solicitudes.perform(get("/api/v1/autenticacion/perfil")).andExpect(status().isUnauthorized());
        solicitudes.perform(get("/api/v1/autenticacion/perfil").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
        solicitudes.perform(get("/api/v1/autenticacion/perfil").header("Authorization", "Bearer " + tokenPrueba("http://emisor-invalido", Instant.now().plusSeconds(60))))
                .andExpect(status().isUnauthorized());
        solicitudes.perform(get("/api/v1/autenticacion/perfil").header("Authorization", "Bearer " + tokenVencido()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilRechazaTokenConRolNoAutorizado() throws Exception {
        solicitudes.perform(get("/api/v1/autenticacion/perfil")
                        .header("Authorization", "Bearer " + tokenConRol("ADMIN")))
                .andExpect(status().isForbidden());
    }

    private String autenticar(String correo, String contrasena) throws Exception {
        return solicitudes.perform(post("/api/v1/autenticacion/iniciar-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoElectronico":"%s","contrasena":"%s"}
                                """.formatted(correo, contrasena)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
    }

    private String registrarUsuario() throws Exception {
        String correo = correoUnico();
        solicitudes.perform(post("/api/v1/autenticacion/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoElectronico":"%s","contrasena":"%s","nombre":"Médico de prueba"}
                                """.formatted(correo, CONTRASENA)))
                .andExpect(status().isCreated());
        return correo;
    }

    private String iniciarSesion(String correo) throws Exception {
        String respuesta = solicitudes.perform(post("/api/v1/autenticacion/iniciar-sesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoElectronico":"%s","contrasena":"%s"}
                                """.formatted(correo, CONTRASENA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.tokenAcceso");
    }

    private String tokenConRol(String rol) {
        return tokenPrueba("http://localhost:8081", Instant.now().plusSeconds(60), rol);
    }

    private String tokenPrueba(String emisor, Instant expiracion) {
        return tokenPrueba(emisor, expiracion, "MEDICO");
    }

    private String tokenPrueba(String emisor, Instant expiracion, String rol) {
        JwtClaimsSet declaraciones = JwtClaimsSet.builder()
                .issuer(emisor)
                .issuedAt(Instant.now())
                .expiresAt(expiracion)
                .subject(UUID.randomUUID().toString())
                .claim("correoElectronico", "prueba@recetaya.cl")
                .claim("nombre", "Usuario de prueba")
                .claim("roles", List.of(rol))
                .build();
        return codificadorJwt.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), declaraciones)).getTokenValue();
    }

    private String tokenVencido() throws Exception {
        String encabezado = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
        String cuerpo = "{\"iss\":\"http://localhost:8081\",\"sub\":\"" + UUID.randomUUID()
                + "\",\"iat\":" + Instant.now().minusSeconds(120).getEpochSecond()
                + ",\"exp\":" + Instant.now().minusSeconds(60).getEpochSecond()
                + ",\"roles\":[\"MEDICO\"]}";
        String cuerpoCodificado = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(cuerpo.getBytes(StandardCharsets.UTF_8));
        String contenidoFirmado = encabezado + "." + cuerpoCodificado;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(claveJwt.getEncoded(), "HmacSHA256"));
        String firma = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(contenidoFirmado.getBytes(StandardCharsets.UTF_8)));
        return contenidoFirmado + "." + firma;
    }

    private String correoUnico() {
        return "prueba-" + UUID.randomUUID() + "@recetaya.cl";
    }
}
