package cl.recetaya.dispensacion.integracion;

import cl.recetaya.dispensacion.excepcion.IntegracionNoDisponibleException;
import java.util.UUID;

public class ValidadorRecetasNoDisponible implements ValidadorRecetas {
    @Override
    public RecetaDispensable consultarRecetaDispensable(UUID recetaId) {
        throw new IntegracionNoDisponibleException(
                "Recetas aún no ofrece una consulta autorizada de recetas reservadas para FARMACEUTICO.");
    }
}
