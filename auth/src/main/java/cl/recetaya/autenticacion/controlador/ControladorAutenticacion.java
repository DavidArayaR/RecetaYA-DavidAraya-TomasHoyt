package cl.recetaya.autenticacion.controlador;

import cl.recetaya.autenticacion.servicio.ServicioAutenticacion;
import cl.recetaya.autenticacion.transferencia.RespuestaToken;
import cl.recetaya.autenticacion.transferencia.SolicitudInicioSesion;
import cl.recetaya.autenticacion.transferencia.SolicitudRegistro;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;

@RestController
public class ControladorAutenticacion {
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/api/v1/autenticacion/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaToken registrar(@Valid @RequestBody SolicitudRegistro solicitud) {
        return servicioAutenticacion.registrar(solicitud);
    }

    @PostMapping({"/api/v1/autenticacion/iniciar-sesion", "/api/v1/auth/login"})
    public RespuestaToken iniciarSesion(@Valid @RequestBody SolicitudInicioSesion solicitud) {
        return servicioAutenticacion.iniciarSesion(solicitud);
    }

    @GetMapping("/api/v1/autenticacion/perfil")
    public RespuestaToken.UsuarioAutenticado perfil(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new RespuestaToken.UsuarioAutenticado(jwt.getSubject(), jwt.getClaimAsString("correoElectronico"),
                jwt.getClaimAsString("nombre"), roles == null ? List.of() : roles);
    }
}
