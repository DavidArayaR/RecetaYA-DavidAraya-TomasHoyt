package cl.recetaya.recetas.servicio;

import cl.recetaya.recetas.entidad.EstadoReceta;
import cl.recetaya.recetas.entidad.Paciente;
import cl.recetaya.recetas.entidad.Receta;
import cl.recetaya.recetas.entidad.RecetaDetalle;
import cl.recetaya.recetas.excepcion.ConflictoRecetaException;
import cl.recetaya.recetas.excepcion.RecursoNoEncontradoException;
import cl.recetaya.recetas.repositorio.RepositorioPaciente;
import cl.recetaya.recetas.repositorio.RepositorioReceta;
import cl.recetaya.recetas.transferencia.RespuestaReceta;
import cl.recetaya.recetas.transferencia.SolicitudReceta;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioRecetas {
    private final RepositorioReceta repositorioReceta;
    private final RepositorioPaciente repositorioPaciente;
    private final Clock reloj;

    public ServicioRecetas(RepositorioReceta repositorioReceta, RepositorioPaciente repositorioPaciente) {
        this.repositorioReceta = repositorioReceta;
        this.repositorioPaciente = repositorioPaciente;
        this.reloj = Clock.systemUTC();
    }

    @Transactional
    public RespuestaReceta crear(SolicitudReceta solicitud, UUID medicoUsuarioId) {
        validarMedicamentos(solicitud.medicamentos());
        Paciente paciente = obtenerOCrearPaciente(solicitud.paciente());
        Receta receta = new Receta(paciente, medicoUsuarioId, solicitud.sucursalId(),
                LocalDateTime.now(reloj));
        solicitud.medicamentos().forEach(m -> receta.agregarDetalle(m.medicamentoId(), m.cantidad()));
        return RespuestaReceta.desde(repositorioReceta.save(receta));
    }

    @Transactional(readOnly = true)
    public RespuestaReceta consultar(UUID recetaId, UUID medicoUsuarioId) {
        return RespuestaReceta.desde(obtenerPropia(recetaId, medicoUsuarioId));
    }

    @Transactional(readOnly = true)
    public List<RespuestaReceta> listar(UUID medicoUsuarioId) {
        return repositorioReceta.findByMedicoUsuarioIdOrderByFechaEmisionDesc(medicoUsuarioId)
                .stream().map(RespuestaReceta::desde).toList();
    }

    @Transactional
    public RespuestaReceta actualizar(UUID recetaId, SolicitudReceta solicitud, UUID medicoUsuarioId) {
        validarMedicamentos(solicitud.medicamentos());
        Receta receta = obtenerPropia(recetaId, medicoUsuarioId);
        exigirPendiente(receta);
        Paciente paciente = obtenerOCrearPaciente(solicitud.paciente());
        receta.actualizar(paciente, solicitud.sucursalId());

        Map<UUID, RecetaDetalle> anteriores = new HashMap<>();
        receta.getDetalles().forEach(detalle -> anteriores.put(detalle.getMedicamentoId(), detalle));
        Set<UUID> nuevosIds = new HashSet<>();
        for (SolicitudReceta.MedicamentoSolicitud medicamento : solicitud.medicamentos()) {
            nuevosIds.add(medicamento.medicamentoId());
            RecetaDetalle anterior = anteriores.get(medicamento.medicamentoId());
            if (anterior != null) {
                anterior.cambiarCantidad(medicamento.cantidad());
            } else {
                receta.agregarDetalle(medicamento.medicamentoId(), medicamento.cantidad());
            }
        }
        receta.getDetalles().removeIf(detalle -> !nuevosIds.contains(detalle.getMedicamentoId()));
        return RespuestaReceta.desde(receta);
    }

    @Transactional
    public RespuestaReceta anular(UUID recetaId, UUID medicoUsuarioId) {
        Receta receta = obtenerPropia(recetaId, medicoUsuarioId);
        exigirPendiente(receta);
        receta.anular();
        return RespuestaReceta.desde(receta);
    }

    private Receta obtenerPropia(UUID recetaId, UUID medicoUsuarioId) {
        return repositorioReceta.findByIdAndMedicoUsuarioId(recetaId, medicoUsuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la receta solicitada."));
    }

    private void exigirPendiente(Receta receta) {
        if (receta.getEstado() != EstadoReceta.ACEPTADA_PENDIENTE_RESERVA) {
            throw new ConflictoRecetaException("Solo se pueden modificar o anular recetas pendientes de reserva.");
        }
    }

    private Paciente obtenerOCrearPaciente(SolicitudReceta.PacienteSolicitud solicitud) {
        String documento = solicitud.documento().trim();
        String nombre = solicitud.nombre().trim();
        return repositorioPaciente.findByDocumento(documento).map(existente -> {
            if (!existente.getNombre().equals(nombre)) {
                throw new ConflictoRecetaException("El documento ya corresponde a un paciente con otro nombre.");
            }
            return existente;
        }).orElseGet(() -> repositorioPaciente.save(new Paciente(documento, nombre)));
    }

    private void validarMedicamentos(List<SolicitudReceta.MedicamentoSolicitud> medicamentos) {
        Set<UUID> identificadores = new HashSet<>();
        for (SolicitudReceta.MedicamentoSolicitud medicamento : medicamentos) {
            if (medicamento == null || medicamento.medicamentoId() == null || medicamento.cantidad() == null
                    || medicamento.cantidad() <= 0) {
                throw new IllegalArgumentException("Cada medicamento debe tener identificador y cantidad positiva.");
            }
            if (!identificadores.add(medicamento.medicamentoId())) {
                throw new ConflictoRecetaException("No se permite repetir un medicamento en una receta.");
            }
        }
    }
}
