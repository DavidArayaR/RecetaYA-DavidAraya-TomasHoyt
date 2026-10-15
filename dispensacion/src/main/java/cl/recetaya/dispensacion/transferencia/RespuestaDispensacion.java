package cl.recetaya.dispensacion.transferencia;

import cl.recetaya.dispensacion.entidad.Dispensacion;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RespuestaDispensacion(
        UUID id, UUID recetaId, UUID reservaId, UUID farmaceuticoUsuarioId,
        String estado, String codigoRechazo, Instant fechaDispensacion, Instant fechaActualizacion,
        List<RespuestaDetalleDispensacion> medicamentos) {
    public static RespuestaDispensacion desde(Dispensacion dispensacion) {
        List<RespuestaDetalleDispensacion> detalles = dispensacion.getDetalles().stream()
                .map(detalle -> new RespuestaDetalleDispensacion(detalle.getMedicamentoId(), detalle.getCantidad()))
                .toList();
        return new RespuestaDispensacion(dispensacion.getId(), dispensacion.getRecetaId(),
                dispensacion.getReservaId(), dispensacion.getFarmaceuticoUsuarioId(),
                dispensacion.getEstado().name(), dispensacion.getCodigoRechazo(), dispensacion.getFechaDispensacion(),
                dispensacion.getFechaActualizacion(), detalles);
    }
}
