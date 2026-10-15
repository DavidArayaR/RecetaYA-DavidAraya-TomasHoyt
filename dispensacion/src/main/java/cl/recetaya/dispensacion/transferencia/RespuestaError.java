package cl.recetaya.dispensacion.transferencia;

import java.time.Instant;

public record RespuestaError(
        Instant fechaHora,
        int codigoEstado,
        String error,
        String mensaje,
        String ruta) {
}
