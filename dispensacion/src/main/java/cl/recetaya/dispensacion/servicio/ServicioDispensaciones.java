package cl.recetaya.dispensacion.servicio;

import cl.recetaya.dispensacion.entidad.Dispensacion;
import cl.recetaya.dispensacion.entidad.EventoInventarioProcesado;
import cl.recetaya.dispensacion.entidad.EstadoDispensacion;
import cl.recetaya.dispensacion.entidad.SolicitudConsumo;
import cl.recetaya.dispensacion.excepcion.ConflictoDispensacionException;
import cl.recetaya.dispensacion.excepcion.RecursoNoEncontradoException;
import cl.recetaya.dispensacion.integracion.DetalleRecetaValidada;
import cl.recetaya.dispensacion.integracion.RecetaDispensable;
import cl.recetaya.dispensacion.integracion.ValidadorRecetas;
import cl.recetaya.dispensacion.repositorio.RepositorioDispensacion;
import cl.recetaya.dispensacion.repositorio.RepositorioEventoInventarioProcesado;
import cl.recetaya.dispensacion.repositorio.RepositorioSolicitudConsumo;
import cl.recetaya.dispensacion.transferencia.EventoSolicitudInventario;
import cl.recetaya.dispensacion.transferencia.RespuestaDispensacion;
import cl.recetaya.dispensacion.transferencia.ResultadoSolicitudInventario;
import cl.recetaya.dispensacion.transferencia.SolicitudDispensacion;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioDispensaciones {
    private final RepositorioDispensacion repositorioDispensacion;
    private final RepositorioSolicitudConsumo repositorioSolicitudConsumo;
    private final RepositorioEventoInventarioProcesado repositorioEventos;
    private final ValidadorRecetas validadorRecetas;
    private final JsonMapper serializador;

    public ServicioDispensaciones(RepositorioDispensacion repositorioDispensacion,
            RepositorioSolicitudConsumo repositorioSolicitudConsumo,
            RepositorioEventoInventarioProcesado repositorioEventos,
            ValidadorRecetas validadorRecetas, JsonMapper serializador) {
        this.repositorioDispensacion = repositorioDispensacion;
        this.repositorioSolicitudConsumo = repositorioSolicitudConsumo;
        this.repositorioEventos = repositorioEventos;
        this.validadorRecetas = validadorRecetas;
        this.serializador = serializador;
    }

    @Transactional
    public RespuestaDispensacion registrar(SolicitudDispensacion solicitud, UUID farmaceuticoId) {
        var existente = repositorioDispensacion.findByRecetaId(solicitud.recetaId());
        if (existente.isPresent()) {
            if (!existente.get().getFarmaceuticoUsuarioId().equals(farmaceuticoId)) {
                throw new AccessDeniedException("La dispensación pertenece a otro usuario farmacéutico.");
            }
            return RespuestaDispensacion.desde(existente.get());
        }

        RecetaDispensable receta = validadorRecetas.consultarRecetaDispensable(solicitud.recetaId());
        validarReceta(solicitud.recetaId(), receta);
        Instant ahora = Instant.now();
        Dispensacion dispensacion = new Dispensacion(receta.recetaId(), receta.reservaId(),
                farmaceuticoId, receta.medicamentos(), ahora);
        UUID mensajeId = UUID.randomUUID();
        UUID correlacionId = UUID.randomUUID();
        EventoSolicitudInventario evento = new EventoSolicitudInventario(
                mensajeId, correlacionId, "CONSUMIR", receta.recetaId(), null, List.of(), ahora);
        try {
            repositorioDispensacion.saveAndFlush(dispensacion);
            repositorioSolicitudConsumo.save(new SolicitudConsumo(mensajeId, correlacionId,
                    receta.recetaId(), serializar(evento), ahora));
        } catch (DataIntegrityViolationException conflicto) {
            throw new ConflictoDispensacionException(
                    "Ya existe una dispensación para la receta o la reserva solicitada.");
        }
        return RespuestaDispensacion.desde(dispensacion);
    }

    @Transactional(readOnly = true)
    public List<RespuestaDispensacion> listar(UUID farmaceuticoId) {
        return repositorioDispensacion.findAllByFarmaceuticoUsuarioIdOrderByFechaDispensacionDesc(farmaceuticoId)
                .stream().map(RespuestaDispensacion::desde).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaDispensacion consultar(UUID id, UUID farmaceuticoId) {
        return RespuestaDispensacion.desde(buscarPropia(id, farmaceuticoId));
    }

    @Transactional(readOnly = true)
    public RespuestaDispensacion consultarEstado(UUID id, UUID farmaceuticoId) {
        return consultar(id, farmaceuticoId);
    }

    @Transactional
    public void procesarResultado(ResultadoSolicitudInventario resultado) {
        if (resultado.eventoId() == null || resultado.solicitudId() == null
                || resultado.correlacionId() == null || resultado.recetaId() == null) {
            throw new ConflictoDispensacionException("El resultado de Inventario no contiene sus identificadores.");
        }
        if (repositorioEventos.existsById(resultado.eventoId())) return;
        SolicitudConsumo solicitud = repositorioSolicitudConsumo.findById(resultado.solicitudId())
                .orElseThrow(() -> new ConflictoDispensacionException("No existe la solicitud de consumo asociada."));
        if (!"CONSUMIR".equals(resultado.tipoOperacion())
                || !solicitud.getCorrelacionId().equals(resultado.correlacionId())
                || !solicitud.getRecetaId().equals(resultado.recetaId())) {
            throw new ConflictoDispensacionException("El resultado no coincide con la solicitud de consumo.");
        }
        Dispensacion dispensacion = repositorioDispensacion.findByRecetaId(resultado.recetaId())
                .orElseThrow(() -> new ConflictoDispensacionException("No existe la dispensación asociada."));
        if (dispensacion.getEstado() == EstadoDispensacion.PENDIENTE_CONSUMO) {
            if ("CONFIRMADA".equals(resultado.resultado())) {
                dispensacion.completar(Instant.now());
            } else if ("RECHAZADA".equals(resultado.resultado())) {
                dispensacion.rechazar(resultado.codigoRechazo() == null
                        ? "RECHAZO_INVENTARIO" : resultado.codigoRechazo(), Instant.now());
            } else {
                throw new ConflictoDispensacionException("Inventario informó un resultado no reconocido.");
            }
        }
        repositorioEventos.save(new EventoInventarioProcesado(resultado.eventoId(),
                resultado.solicitudId(), resultado.correlacionId(), resultado.recetaId(), Instant.now()));
    }

    private Dispensacion buscarPropia(UUID id, UUID farmaceuticoId) {
        return repositorioDispensacion.findByIdAndFarmaceuticoUsuarioId(id, farmaceuticoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la dispensación solicitada."));
    }

    private void validarReceta(UUID recetaId, RecetaDispensable receta) {
        if (receta == null || !recetaId.equals(receta.recetaId()) || receta.reservaId() == null
                || !"RESERVADA".equals(receta.estado()) || receta.medicamentos() == null
                || receta.medicamentos().isEmpty()) {
            throw new ConflictoDispensacionException("La receta no está reservada o no puede dispensarse.");
        }
        HashSet<UUID> medicamentos = new HashSet<>();
        for (DetalleRecetaValidada detalle : receta.medicamentos()) {
            if (detalle == null || detalle.medicamentoId() == null || detalle.cantidad() <= 0
                    || !medicamentos.add(detalle.medicamentoId())) {
                throw new ConflictoDispensacionException("Los medicamentos o cantidades de la receta no son válidos.");
            }
        }
    }

    private String serializar(EventoSolicitudInventario evento) {
        try {
            return serializador.writeValueAsString(evento);
        } catch (JacksonException excepcion) {
            throw new IllegalStateException("No se pudo preparar el evento de consumo.", excepcion);
        }
    }
}
