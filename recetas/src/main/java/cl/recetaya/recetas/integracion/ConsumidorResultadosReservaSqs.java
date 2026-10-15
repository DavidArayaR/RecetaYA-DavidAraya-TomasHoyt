package cl.recetaya.recetas.integracion;

import cl.recetaya.recetas.servicio.ServicioResultadosReserva;
import cl.recetaya.recetas.transferencia.ResultadoSolicitudInventario;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@ConditionalOnProperty(prefix = "recetaya.integracion.sqs", name = "habilitada", havingValue = "true")
public class ConsumidorResultadosReservaSqs {
    private static final Logger REGISTRO = LoggerFactory.getLogger(ConsumidorResultadosReservaSqs.class);

    private final SqsClient clienteSqs;
    private final ObjectMapper serializador;
    private final ServicioResultadosReserva servicioResultados;
    private final String colaResultadosRecetas;

    public ConsumidorResultadosReservaSqs(SqsClient clienteSqs, ObjectMapper serializador,
            ServicioResultadosReserva servicioResultados,
            @Value("${recetaya.integracion.sqs.resultados-recetas:}") String colaResultadosRecetas) {
        this.clienteSqs = clienteSqs;
        this.serializador = serializador;
        this.servicioResultados = servicioResultados;
        this.colaResultadosRecetas = colaResultadosRecetas;
    }

    @Scheduled(fixedDelayString = "${recetaya.integracion.sqs.demora-ms:3000}")
    public void recibirResultados() {
        if (colaResultadosRecetas.isBlank()) {
            return;
        }
        List<Message> mensajes = clienteSqs.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(colaResultadosRecetas)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(1)
                .build()).messages();
        for (Message mensaje : mensajes) {
            try {
                ResultadoSolicitudInventario resultado = serializador.readValue(
                        mensaje.body(), ResultadoSolicitudInventario.class);
                servicioResultados.procesar(resultado);
                clienteSqs.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(colaResultadosRecetas)
                        .receiptHandle(mensaje.receiptHandle())
                        .build());
            } catch (Exception error) {
                REGISTRO.warn("No se pudo procesar un resultado de Inventario; SQS lo reintentará.");
            }
        }
    }
}
