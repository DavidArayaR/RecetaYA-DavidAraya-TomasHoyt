package cl.recetaya.dispensacion.controlador;

import cl.recetaya.dispensacion.servicio.ServicioDispensaciones;
import cl.recetaya.dispensacion.transferencia.RespuestaDispensacion;
import cl.recetaya.dispensacion.transferencia.RespuestaDetalleDispensacion;
import cl.recetaya.dispensacion.transferencia.SolicitudDispensacion;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispensaciones")
public class ControladorDispensaciones {
    private final ServicioDispensaciones servicioDispensaciones;

    public ControladorDispensaciones(ServicioDispensaciones servicioDispensaciones) {
        this.servicioDispensaciones = servicioDispensaciones;
    }

    @PostMapping
    public ResponseEntity<RespuestaDispensacion> registrar(@Valid @RequestBody SolicitudDispensacion solicitud,
                                                            @AuthenticationPrincipal Jwt usuario) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(servicioDispensaciones.registrar(
                solicitud, UUID.fromString(usuario.getSubject())));
    }

    @GetMapping
    public List<RespuestaDispensacion> listar(@AuthenticationPrincipal Jwt usuario) {
        return servicioDispensaciones.listar(UUID.fromString(usuario.getSubject()));
    }

    @GetMapping("/{id}")
    public RespuestaDispensacion consultar(@PathVariable UUID id, @AuthenticationPrincipal Jwt usuario) {
        return servicioDispensaciones.consultar(id, UUID.fromString(usuario.getSubject()));
    }

    @GetMapping("/{id}/estado")
    public RespuestaDispensacion consultarEstado(@PathVariable UUID id, @AuthenticationPrincipal Jwt usuario) {
        return servicioDispensaciones.consultarEstado(id, UUID.fromString(usuario.getSubject()));
    }

    @GetMapping("/{id}/detalle")
    public List<RespuestaDetalleDispensacion> consultarDetalle(@PathVariable UUID id,
                                                               @AuthenticationPrincipal Jwt usuario) {
        return servicioDispensaciones.consultar(id, UUID.fromString(usuario.getSubject())).medicamentos();
    }
}
