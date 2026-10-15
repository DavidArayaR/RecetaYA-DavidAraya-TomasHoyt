package cl.recetaya.dispensacion.servicio;

import cl.recetaya.dispensacion.entidad.EstadoSolicitudConsumo;
import cl.recetaya.dispensacion.entidad.SolicitudConsumo;
import cl.recetaya.dispensacion.repositorio.RepositorioSolicitudConsumo;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(name = "recetaya.integracion.inventario.sqs-habilitada", havingValue = "true")
public class ServicioPublicacionConsumoSqs {
    private final RepositorioSolicitudConsumo repositorio;
    private final SqsClient clienteSqs;
    private final String colaSolicitudes;

    public ServicioPublicacionConsumoSqs(RepositorioSolicitudConsumo repositorio, SqsClient clienteSqs,
            @Value("${recetaya.integracion.inventario.solicitudes:}") String colaSolicitudes) {
        this.repositorio = repositorio;
        this.clienteSqs = clienteSqs;
        this.colaSolicitudes = colaSolicitudes;
    }

    @Scheduled(fixedDelayString = "${RECETAYA_SQS_INTERVALO_MS:5000}")
    public void publicarPendientes() {
        if (colaSolicitudes.isBlank()) return;
        List<SolicitudConsumo> pendientes = repositorio
                .findTop50ByEstadoAndProximoIntentoLessThanEqualOrderByFechaCreacion(
                        EstadoSolicitudConsumo.PENDIENTE, Instant.now());
        for (SolicitudConsumo solicitud : pendientes) {
            try {
                clienteSqs.sendMessage(SendMessageRequest.builder()
                        .queueUrl(colaSolicitudes)
                        .messageBody(solicitud.getContenido())
                        .build());
                marcarPublicada(solicitud);
            } catch (RuntimeException error) {
                registrarFallo(solicitud, error);
            }
        }
    }

    @Transactional
    protected void marcarPublicada(SolicitudConsumo solicitud) {
        SolicitudConsumo actual = repositorio.findById(solicitud.getMensajeId()).orElse(null);
        if (actual != null && actual.getEstado() == EstadoSolicitudConsumo.PENDIENTE) {
            actual.marcarPublicada();
            repositorio.save(actual);
        }
    }

    @Transactional
    protected void registrarFallo(SolicitudConsumo solicitud, RuntimeException error) {
        SolicitudConsumo actual = repositorio.findById(solicitud.getMensajeId()).orElse(null);
        if (actual == null || actual.getEstado() != EstadoSolicitudConsumo.PENDIENTE) return;
        int siguienteIntento = actual.getIntentos() + 1;
        boolean definitivo = siguienteIntento >= 5;
        long espera = Math.min(60, 1L << Math.min(siguienteIntento, 6));
        actual.registrarFallo(error.getClass().getSimpleName(), Instant.now().plusSeconds(espera), definitivo);
        repositorio.save(actual);
    }
}
