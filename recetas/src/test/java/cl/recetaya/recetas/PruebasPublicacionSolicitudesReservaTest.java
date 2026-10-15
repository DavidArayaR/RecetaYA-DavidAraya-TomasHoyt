package cl.recetaya.recetas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.recetaya.recetas.entidad.EstadoPublicacionReserva;
import cl.recetaya.recetas.entidad.Paciente;
import cl.recetaya.recetas.entidad.Receta;
import cl.recetaya.recetas.entidad.SolicitudReserva;
import cl.recetaya.recetas.repositorio.RepositorioSolicitudReserva;
import cl.recetaya.recetas.servicio.ServicioPublicacionSolicitudesReserva;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@ExtendWith(MockitoExtension.class)
class PruebasPublicacionSolicitudesReservaTest {
    @Mock
    RepositorioSolicitudReserva repositorio;

    @Mock
    SqsClient clienteSqs;

    private SolicitudReserva solicitud;
    private ServicioPublicacionSolicitudesReserva publicador;

    @BeforeEach
    void preparar() {
        Receta receta = new Receta(new Paciente("PAC-201", "Paciente de prueba"), UUID.randomUUID(),
                UUID.randomUUID(), LocalDateTime.now());
        receta.agregarDetalle(UUID.randomUUID(), 2);
        solicitud = new SolicitudReserva(receta, UUID.randomUUID(), UUID.randomUUID(),
                "{\"tipoOperacion\":\"RESERVAR\"}", Instant.now());
        publicador = new ServicioPublicacionSolicitudesReserva(repositorio, clienteSqs,
                "https://sqs.example.test/solicitudes", 5);
    }

    @Test
    void publicaSolicitudPendienteYMarcaEstado() {
        when(repositorio.buscarPendientes(any(), any(), any())).thenReturn(List.of(solicitud));

        publicador.publicarPendientes();

        assertEquals(EstadoPublicacionReserva.PUBLICADO, solicitud.getEstadoPublicacion());
        assertEquals(1, solicitud.getIntentos());
        verify(clienteSqs).sendMessage(argThat((SendMessageRequest solicitudSqs) ->
                solicitudSqs.messageBody().contains("RESERVAR")));
        verify(repositorio).save(solicitud);
    }

    @Test
    void programaReintentoSinGuardarDetallesDeExcepcion() {
        when(repositorio.buscarPendientes(any(), any(), any())).thenReturn(List.of(solicitud));
        when(clienteSqs.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().message("Falla de prueba").build());

        publicador.publicarPendientes();

        assertEquals(EstadoPublicacionReserva.PENDIENTE, solicitud.getEstadoPublicacion());
        assertEquals(1, solicitud.getIntentos());
        assertEquals("No se pudo publicar la solicitud en la cola de Inventario.",
                solicitud.getErrorPublicacion());
        verify(repositorio).save(solicitud);
        verify(clienteSqs).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void dejaDeReintentarAlAlcanzarElLimiteConfigurado() {
        for (int intento = 0; intento < 5; intento++) {
            solicitud.registrarErrorPublicacion(Instant.now(), 5);
        }

        assertEquals(EstadoPublicacionReserva.FALLIDO, solicitud.getEstadoPublicacion());
        assertEquals(5, solicitud.getIntentos());
        verify(clienteSqs, never()).sendMessage(any(SendMessageRequest.class));
    }
}
