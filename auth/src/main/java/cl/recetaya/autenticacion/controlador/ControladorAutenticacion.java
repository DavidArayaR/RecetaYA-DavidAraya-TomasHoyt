package cl.recetaya.autenticacion.controlador;

import cl.recetaya.autenticacion.servicio.ServicioAutenticacion;
import cl.recetaya.autenticacion.transferencia.RespuestaToken;
import cl.recetaya.autenticacion.transferencia.SolicitudInicioSesion;
import cl.recetaya.autenticacion.transferencia.SolicitudRegistro;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/autenticacion")
public class ControladorAutenticacion {
    private final ServicioAutenticacion servicioAutenticacion;

    public ControladorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaToken registrar(@Valid @RequestBody SolicitudRegistro solicitud) {
        return servicioAutenticacion.registrar(solicitud);
    }

    @PostMapping("/iniciar-sesion")
    public RespuestaToken iniciarSesion(@Valid @RequestBody SolicitudInicioSesion solicitud) {
        return servicioAutenticacion.iniciarSesion(solicitud);
    }
}
