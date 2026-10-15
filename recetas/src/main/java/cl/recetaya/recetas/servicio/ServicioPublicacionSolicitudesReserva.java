package cl.recetaya.recetas.servicio;

import cl.recetaya.recetas.entidad.EstadoPublicacionReserva;
import cl.recetaya.recetas.entidad.SolicitudReserva;
import cl.recetaya.recetas.repositorio.RepositorioSolicitudReserva;
import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(prefix = "recetaya.integracion.sqs", name = "habilitada", havingValue = "true")
public class ServicioPublicacionSolicitudesReserva {
    private static final Logger REGISTRO = LoggerFactory.getLogger(ServicioPublicacionSolicitudesReserva.class);

    private final RepositorioSolicitudReserva repositorio;
    private final SqsClient clienteSqs;
    private final String colaSolicitudes;
    private final int maximoIntentos;
    private final Clock reloj = Clock.systemUTC();

    public ServicioPublicacionSolicitudesReserva(
            RepositorioSolicitudReserva repositorio,
            SqsClient clienteSqs,
            @Value("${recetaya.integracion.sqs.solicitudes:}") String colaSolicitudes,
            @Value("${recetaya.integracion.sqs.max-intentos:5}") int maximoIntentos) {
        if (colaSolicitudes.isBlank()) {
            throw new IllegalStateException("RECETAYA_SQS_SOLICITUDES es obligatorio cuando SQS está habilitada.");
        }
        if (maximoIntentos < 1) {
            throw new IllegalStateException("El máximo de intentos SQS debe ser positivo.");
        }
        this.repositorio = repositorio;
        this.clienteSqs = clienteSqs;
        this.colaSolicitudes = colaSolicitudes;
        this.maximoIntentos = maximoIntentos;
    }

    @Scheduled(fixedDelayString = "${recetaya.integracion.sqs.demora-ms:3000}")
    public void publicarPendientes() {
        List<SolicitudReserva> pendientes = repositorio.buscarPendientes(
                EstadoPublicacionReserva.PENDIENTE, reloj.instant(), PageRequest.of(0, 20));
        for (SolicitudReserva solicitud : pendientes) {
            try {
                clienteSqs.sendMessage(SendMessageRequest.builder()
                        .queueUrl(colaSolicitudes)
                        .messageBody(solicitud.getCuerpoJson())
                        .build());
                solicitud.marcarPublicado(reloj.instant());
            } catch (RuntimeException error) {
                solicitud.registrarErrorPublicacion(reloj.instant(), maximoIntentos);
                REGISTRO.warn("No se publicó la solicitud de reserva {} (intento {}).",
                        solicitud.getMensajeId(), solicitud.getIntentos());
            }
            repositorio.save(solicitud);
        }
    }
}
