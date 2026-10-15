package cl.recetaya.dispensacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.recetaya.dispensacion.entidad.EstadoSolicitudConsumo;
import cl.recetaya.dispensacion.entidad.SolicitudConsumo;
import cl.recetaya.dispensacion.repositorio.RepositorioSolicitudConsumo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

class PruebasPublicacionConsumoSqsTest {
    @Test
    void publicaElMensajePersistidoYMarcaLaSolicitud() {
        RepositorioSolicitudConsumo repositorio = mock(RepositorioSolicitudConsumo.class);
        SqsClient cliente = mock(SqsClient.class);
        UUID mensajeId = UUID.randomUUID();
        SolicitudConsumo solicitud = new SolicitudConsumo(mensajeId, UUID.randomUUID(),
                UUID.randomUUID(), "{\"tipoOperacion\":\"CONSUMIR\"}", Instant.now());
        when(repositorio.findTop50ByEstadoAndProximoIntentoLessThanEqualOrderByFechaCreacion(
                any(), any())).thenReturn(List.of(solicitud));
        when(repositorio.findById(mensajeId)).thenReturn(Optional.of(solicitud));
        ServicioPublicacionConsumoSqs publicador = new ServicioPublicacionConsumoSqs(
                repositorio, cliente, "https://sqs.local/solicitudes");

        publicador.publicarPendientes();

        ArgumentCaptor<SendMessageRequest> mensaje = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(cliente).sendMessage(mensaje.capture());
        assertEquals("{\"tipoOperacion\":\"CONSUMIR\"}", mensaje.getValue().messageBody());
        assertEquals(EstadoSolicitudConsumo.PUBLICADA, solicitud.getEstado());
        verify(repositorio).save(solicitud);
    }
}
