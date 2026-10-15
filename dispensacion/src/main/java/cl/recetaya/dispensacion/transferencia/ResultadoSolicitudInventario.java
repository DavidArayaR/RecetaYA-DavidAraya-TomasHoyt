package cl.recetaya.dispensacion.transferencia;

import java.time.Instant;
import java.util.UUID;

public record ResultadoSolicitudInventario(
        UUID eventoId,
        UUID solicitudId,
        UUID correlacionId,
        String tipoOperacion,
        UUID recetaId,
        String resultado,
        String codigoRechazo,
        String mensaje,
        Object reserva,
        Instant fechaHora) {
}
