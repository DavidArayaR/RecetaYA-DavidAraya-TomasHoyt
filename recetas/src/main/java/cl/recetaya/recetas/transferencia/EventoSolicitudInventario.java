package cl.recetaya.recetas.transferencia;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventoSolicitudInventario(
        UUID mensajeId,
        UUID correlacionId,
        String tipoOperacion,
        UUID recetaId,
        UUID sucursalId,
        List<Medicamento> medicamentos,
        Instant fechaHora
) {
    public record Medicamento(UUID medicamentoId, int cantidad) { }
}
