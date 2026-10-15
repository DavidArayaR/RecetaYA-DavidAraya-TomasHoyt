package cl.recetaya.recetas.servicio;

import cl.recetaya.recetas.entidad.EventoInventarioProcesado;
import cl.recetaya.recetas.entidad.Receta;
import cl.recetaya.recetas.entidad.SolicitudReserva;
import cl.recetaya.recetas.excepcion.ConflictoRecetaException;
import cl.recetaya.recetas.excepcion.RecursoNoEncontradoException;
import cl.recetaya.recetas.repositorio.RepositorioEventoInventarioProcesado;
import cl.recetaya.recetas.repositorio.RepositorioReceta;
import cl.recetaya.recetas.repositorio.RepositorioSolicitudReserva;
import cl.recetaya.recetas.transferencia.ResultadoSolicitudInventario;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioResultadosReserva {
    private final RepositorioSolicitudReserva repositorioSolicitudes;
    private final RepositorioReceta repositorioRecetas;
    private final RepositorioEventoInventarioProcesado repositorioEventos;
    private final Clock reloj = Clock.systemUTC();

    public ServicioResultadosReserva(RepositorioSolicitudReserva repositorioSolicitudes,
                                     RepositorioReceta repositorioRecetas,
                                     RepositorioEventoInventarioProcesado repositorioEventos) {
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.repositorioRecetas = repositorioRecetas;
        this.repositorioEventos = repositorioEventos;
    }

    @Transactional
    public void procesar(ResultadoSolicitudInventario resultado) {
        validar(resultado);
        if (repositorioEventos.existsById(resultado.eventoId())) {
            return;
        }

        SolicitudReserva solicitud = repositorioSolicitudes.buscarPorCorrelacionId(resultado.correlacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud correlacionada."));
        if (!solicitud.getMensajeId().equals(resultado.solicitudId())
                || !solicitud.getReceta().getId().equals(resultado.recetaId())) {
            throw new ConflictoRecetaException("El resultado no coincide con la solicitud de reserva registrada.");
        }

        Receta receta = repositorioRecetas.findById(resultado.recetaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la receta del resultado."));
        boolean confirmada = "CONFIRMADA".equals(resultado.resultado());
        if (!confirmada && !"RECHAZADA".equals(resultado.resultado())) {
            throw new ConflictoRecetaException("El resultado de Inventario no está reconocido.");
        }
        receta.registrarResultadoReserva(confirmada, normalizarCodigo(resultado.codigoRechazo()));
        repositorioEventos.save(new EventoInventarioProcesado(resultado.eventoId(),
                resultado.correlacionId(), reloj.instant()));
    }

    private void validar(ResultadoSolicitudInventario resultado) {
        if (resultado == null || resultado.eventoId() == null || resultado.solicitudId() == null
                || resultado.correlacionId() == null || resultado.recetaId() == null
                || !"RESERVAR".equals(resultado.tipoOperacion())) {
            throw new ConflictoRecetaException("El resultado de Inventario no contiene identificadores válidos.");
        }
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String normalizado = codigo.trim();
        if (!normalizado.matches("[A-Z0-9_]{1,64}")) {
            throw new ConflictoRecetaException("El código de rechazo de Inventario no es válido.");
        }
        return normalizado;
    }
}
