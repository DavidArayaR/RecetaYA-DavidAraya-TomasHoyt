package cl.recetaya.dispensacion.integracion;

import java.util.UUID;

public interface ValidadorRecetas {
    RecetaDispensable consultarRecetaDispensable(UUID recetaId);
}
