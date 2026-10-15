package cl.recetaya.dispensacion.controlador;

import cl.recetaya.dispensacion.servicio.ServicioDispensaciones;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispensaciones")
public class ControladorDispensaciones {
    private final ServicioDispensaciones servicioDispensaciones;

    public ControladorDispensaciones(ServicioDispensaciones servicioDispensaciones) {
        this.servicioDispensaciones = servicioDispensaciones;
    }

    @GetMapping
    public void listar() {
        servicioDispensaciones.listar();
    }
}
