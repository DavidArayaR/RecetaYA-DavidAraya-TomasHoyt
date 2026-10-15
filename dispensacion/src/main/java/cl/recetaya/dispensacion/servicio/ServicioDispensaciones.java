package cl.recetaya.dispensacion.servicio;

import cl.recetaya.dispensacion.excepcion.FuncionalidadNoImplementadaException;
import org.springframework.stereotype.Service;

@Service
public class ServicioDispensaciones {
    public void listar() {
        throw new FuncionalidadNoImplementadaException();
    }
}
