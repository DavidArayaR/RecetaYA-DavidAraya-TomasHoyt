package cl.recetaya.recetas.controlador;

import cl.recetaya.recetas.servicio.ServicioRecetas;
import cl.recetaya.recetas.transferencia.RespuestaReceta;
import cl.recetaya.recetas.transferencia.SolicitudReceta;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recetas")
public class ControladorRecetas {
    private final ServicioRecetas servicioRecetas;

    public ControladorRecetas(ServicioRecetas servicioRecetas) {
        this.servicioRecetas = servicioRecetas;
    }

    @PostMapping
    public ResponseEntity<RespuestaReceta> crear(@Valid @RequestBody SolicitudReceta solicitud,
                                                 @AuthenticationPrincipal Jwt usuario) {
        RespuestaReceta respuesta = servicioRecetas.crear(solicitud, UUID.fromString(usuario.getSubject()));
        return ResponseEntity.created(URI.create("/api/v1/recetas/" + respuesta.id())).body(respuesta);
    }

    @GetMapping
    public List<RespuestaReceta> listar(@AuthenticationPrincipal Jwt usuario) {
        return servicioRecetas.listar(UUID.fromString(usuario.getSubject()));
    }

    @GetMapping("/{id}")
    public RespuestaReceta consultar(@PathVariable UUID id, @AuthenticationPrincipal Jwt usuario) {
        return servicioRecetas.consultar(id, UUID.fromString(usuario.getSubject()));
    }

    @PutMapping("/{id}")
    public RespuestaReceta actualizar(@PathVariable UUID id, @Valid @RequestBody SolicitudReceta solicitud,
                                      @AuthenticationPrincipal Jwt usuario) {
        return servicioRecetas.actualizar(id, solicitud, UUID.fromString(usuario.getSubject()));
    }

    @PostMapping("/{id}/anular")
    public RespuestaReceta anular(@PathVariable UUID id, @AuthenticationPrincipal Jwt usuario) {
        return servicioRecetas.anular(id, UUID.fromString(usuario.getSubject()));
    }
}
