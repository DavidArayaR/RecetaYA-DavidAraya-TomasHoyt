package cl.recetaya.dispensacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.recetaya.dispensacion.entidad.Dispensacion;
import cl.recetaya.dispensacion.entidad.EventoInventarioProcesado;
import cl.recetaya.dispensacion.entidad.SolicitudConsumo;
import cl.recetaya.dispensacion.excepcion.ConflictoDispensacionException;
import cl.recetaya.dispensacion.integracion.DetalleRecetaValidada;
import cl.recetaya.dispensacion.integracion.RecetaDispensable;
import cl.recetaya.dispensacion.integracion.ValidadorRecetas;
import cl.recetaya.dispensacion.repositorio.RepositorioDispensacion;
import cl.recetaya.dispensacion.repositorio.RepositorioEventoInventarioProcesado;
import cl.recetaya.dispensacion.repositorio.RepositorioSolicitudConsumo;
import cl.recetaya.dispensacion.transferencia.ResultadoSolicitudInventario;
import cl.recetaya.dispensacion.transferencia.SolicitudDispensacion;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PruebasServicioDispensacionesTest {
    @Mock RepositorioDispensacion dispensaciones;
    @Mock RepositorioSolicitudConsumo solicitudes;
    @Mock RepositorioEventoInventarioProcesado eventos;
    @Mock ValidadorRecetas validador;

    private ServicioDispensaciones servicio;
    private UUID recetaId;
    private UUID farmaceuticoId;
    private UUID medicamentoId;
    private UUID reservaId;

    @BeforeEach
    void preparar() {
        servicio = new ServicioDispensaciones(dispensaciones, solicitudes, eventos, validador, JsonMapper.builder().build());
        recetaId = UUID.randomUUID();
        farmaceuticoId = UUID.randomUUID();
        medicamentoId = UUID.randomUUID();
        reservaId = UUID.randomUUID();
    }

    @Test
    void registraUnaDispensacionPendienteYPreparaConsumo() {
        when(dispensaciones.findByRecetaId(recetaId)).thenReturn(Optional.empty());
        when(validador.consultarRecetaDispensable(recetaId)).thenReturn(
                new RecetaDispensable(recetaId, reservaId, UUID.randomUUID(), "RESERVADA",
                        List.of(new DetalleRecetaValidada(medicamentoId, 3))));

        var respuesta = servicio.registrar(new SolicitudDispensacion(recetaId), farmaceuticoId);

        assertEquals("PENDIENTE_CONSUMO", respuesta.estado());
        assertEquals(3, respuesta.medicamentos().getFirst().cantidad());
        ArgumentCaptor<SolicitudConsumo> captor = ArgumentCaptor.forClass(SolicitudConsumo.class);
        verify(solicitudes).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().getContenido().contains("CONSUMIR"));
    }

    @Test
    void noRegistraSiLaRecetaNoEstaReservada() {
        when(dispensaciones.findByRecetaId(recetaId)).thenReturn(Optional.empty());
        when(validador.consultarRecetaDispensable(recetaId)).thenReturn(
                new RecetaDispensable(recetaId, reservaId, UUID.randomUUID(), "ACEPTADA_PENDIENTE_RESERVA",
                        List.of(new DetalleRecetaValidada(medicamentoId, 1))));

        assertThrows(ConflictoDispensacionException.class,
                () -> servicio.registrar(new SolicitudDispensacion(recetaId), farmaceuticoId));
    }

    @Test
    void procesaConfirmacionYDeduplicaElMismoEvento() {
        UUID mensajeId = UUID.randomUUID();
        UUID correlacionId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        Dispensacion dispensacion = new Dispensacion(recetaId, reservaId, farmaceuticoId,
                List.of(new DetalleRecetaValidada(medicamentoId, 1)), Instant.now());
        SolicitudConsumo solicitud = new SolicitudConsumo(mensajeId, correlacionId, recetaId, "{}", Instant.now());
        var resultado = new ResultadoSolicitudInventario(eventoId, mensajeId, correlacionId,
                "CONSUMIR", recetaId, "CONFIRMADA", null, null, null, Instant.now());
        when(eventos.existsById(eventoId)).thenReturn(false, true);
        when(solicitudes.findById(mensajeId)).thenReturn(Optional.of(solicitud));
        when(dispensaciones.findByRecetaId(recetaId)).thenReturn(Optional.of(dispensacion));

        servicio.procesarResultado(resultado);
        servicio.procesarResultado(resultado);

        assertEquals("COMPLETADA", dispensacion.getEstado().name());
        verify(eventos).save(any(EventoInventarioProcesado.class));
    }

    @Test
    void rechazaResultadosConCorrelacionAjena() {
        UUID mensajeId = UUID.randomUUID();
        UUID correlacionId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        when(eventos.existsById(eventoId)).thenReturn(false);
        when(solicitudes.findById(mensajeId)).thenReturn(Optional.of(
                new SolicitudConsumo(mensajeId, correlacionId, recetaId, "{}", Instant.now())));
        var resultado = new ResultadoSolicitudInventario(eventoId, mensajeId, UUID.randomUUID(),
                "CONSUMIR", recetaId, "CONFIRMADA", null, null, null, Instant.now());

        assertThrows(ConflictoDispensacionException.class, () -> servicio.procesarResultado(resultado));
    }
}
