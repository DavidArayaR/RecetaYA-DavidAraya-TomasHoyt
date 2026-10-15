package cl.recetaya.recetas.servicio;

import cl.recetaya.recetas.entidad.EstadoPublicacionReserva;
import cl.recetaya.recetas.entidad.Receta;
import cl.recetaya.recetas.entidad.SolicitudReserva;
import cl.recetaya.recetas.excepcion.ConflictoRecetaException;
import cl.recetaya.recetas.excepcion.RecursoNoEncontradoException;
import cl.recetaya.recetas.repositorio.RepositorioSolicitudReserva;
import cl.recetaya.recetas.transferencia.EventoSolicitudInventario;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioSolicitudesReserva {
    private final RepositorioSolicitudReserva repositorio;
    private final ObjectMapper serializador;
    private final Clock reloj;

    public ServicioSolicitudesReserva(RepositorioSolicitudReserva repositorio, ObjectMapper serializador) {
        this.repositorio = repositorio;
        this.serializador = serializador;
        this.reloj = Clock.systemUTC();
    }

    @Transactional
    public void crearPara(Receta receta) {
        Instant ahora = reloj.instant();
        UUID mensajeId = UUID.randomUUID();
        UUID correlacionId = UUID.randomUUID();
        repositorio.save(new SolicitudReserva(receta, mensajeId, correlacionId,
                serializar(evento(receta, mensajeId, correlacionId, ahora)), ahora));
    }

    @Transactional
    public void actualizarPendiente(Receta receta) {
        SolicitudReserva solicitud = obtenerPendiente(receta.getId());
        solicitud.actualizarCuerpoPendiente(serializar(evento(receta, solicitud.getMensajeId(),
                solicitud.getCorrelacionId(), solicitud.getFechaCreacion())));
    }

    @Transactional
    public void cancelarPendiente(UUID recetaId) {
        SolicitudReserva solicitud = obtenerPendiente(recetaId);
        try {
            solicitud.cancelarPendiente();
        } catch (IllegalStateException error) {
            throw new ConflictoRecetaException(error.getMessage());
        }
    }

    private SolicitudReserva obtenerPendiente(UUID recetaId) {
        SolicitudReserva solicitud = repositorio.buscarPorRecetaId(recetaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud de reserva."));
        if (solicitud.getEstadoPublicacion() != EstadoPublicacionReserva.PENDIENTE) {
            throw new ConflictoRecetaException("La solicitud de reserva ya fue publicada o no admite cambios.");
        }
        return solicitud;
    }

    private EventoSolicitudInventario evento(Receta receta, UUID mensajeId, UUID correlacionId, Instant fechaHora) {
        return new EventoSolicitudInventario(mensajeId, correlacionId, "RESERVAR", receta.getId(),
                receta.getSucursalId(), receta.getDetalles().stream()
                        .map(detalle -> new EventoSolicitudInventario.Medicamento(
                                detalle.getMedicamentoId(), detalle.getCantidad()))
                        .toList(), fechaHora);
    }

    private String serializar(EventoSolicitudInventario evento) {
        try {
            return serializador.writeValueAsString(evento);
        } catch (JacksonException error) {
            throw new IllegalStateException("No se pudo preparar la solicitud de reserva.", error);
        }
    }
}
