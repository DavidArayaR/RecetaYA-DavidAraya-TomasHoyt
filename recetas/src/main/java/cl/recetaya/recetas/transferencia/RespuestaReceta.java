package cl.recetaya.recetas.transferencia;

import cl.recetaya.recetas.entidad.EstadoReceta;
import cl.recetaya.recetas.entidad.Receta;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public record RespuestaReceta(
        UUID id,
        PacienteRespuesta paciente,
        UUID medicoUsuarioId,
        UUID sucursalId,
        EstadoReceta estado,
        Instant fechaEmision,
        List<MedicamentoRespuesta> medicamentos
) {
    public record PacienteRespuesta(UUID id, String documento, String nombre) { }
    public record MedicamentoRespuesta(UUID medicamentoId, int cantidad) { }

    public static RespuestaReceta desde(Receta receta) {
        return new RespuestaReceta(
                receta.getId(),
                new PacienteRespuesta(receta.getPaciente().getId(),
                        receta.getPaciente().getDocumento(), receta.getPaciente().getNombre()),
                receta.getMedicoUsuarioId(),
                receta.getSucursalId(),
                receta.getEstado(),
                receta.getFechaEmision().toInstant(ZoneOffset.UTC),
                receta.getDetalles().stream()
                        .map(detalle -> new MedicamentoRespuesta(detalle.getMedicamentoId(), detalle.getCantidad()))
                        .toList()
        );
    }
}
