package cl.recetaya.dispensacion.integracion;

import java.util.List;
import java.util.UUID;

public record RecetaDispensable(
        UUID recetaId,
        UUID reservaId,
        UUID sucursalId,
        String estado,
        List<DetalleRecetaValidada> medicamentos) {
}
