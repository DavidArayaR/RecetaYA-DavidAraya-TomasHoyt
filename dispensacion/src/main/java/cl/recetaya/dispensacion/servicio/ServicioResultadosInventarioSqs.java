package cl.recetaya.dispensacion.servicio;

import cl.recetaya.dispensacion.transferencia.ResultadoSolicitudInventario;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@ConditionalOnProperty(name = "recetaya.integracion.inventario.sqs-habilitada", havingValue = "true")
public class ServicioResultadosInventarioSqs {
    private static final Logger REGISTRO = LoggerFactory.getLogger(ServicioResultadosInventarioSqs.class);
    private final SqsClient clienteSqs;
    private final JsonMapper serializador;
    private final ServicioDispensaciones servicio;
    private final String colaResultados;

    public ServicioResultadosInventarioSqs(SqsClient clienteSqs, JsonMapper serializador,
            ServicioDispensaciones servicio,
            @Value("${recetaya.integracion.inventario.resultados-dispensacion:}") String colaResultados) {
        this.clienteSqs = clienteSqs;
        this.serializador = serializador;
        this.servicio = servicio;
        this.colaResultados = colaResultados;
    }

    @Scheduled(fixedDelayString = "${RECETAYA_SQS_INTERVALO_MS:5000}", initialDelay = 1000)
    public void procesarResultados() {
        if (colaResultados.isBlank()) return;
        List<Message> mensajes = clienteSqs.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(colaResultados).maxNumberOfMessages(10).waitTimeSeconds(1)
                .visibilityTimeout(30).build()).messages();
        for (Message mensaje : mensajes) {
            try {
                ResultadoSolicitudInventario resultado = serializador.readValue(
                        mensaje.body(), ResultadoSolicitudInventario.class);
                servicio.procesarResultado(resultado);
                clienteSqs.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(colaResultados).receiptHandle(mensaje.receiptHandle()).build());
            } catch (Exception excepcion) {
                // La cola de mensajes fallidos configurada en AWS controla los mensajes persistentes.
                REGISTRO.warn("No se procesó el mensaje de resultado SQS {}: {}",
                        mensaje.messageId(), excepcion.getClass().getSimpleName());
            }
        }
    }
}
