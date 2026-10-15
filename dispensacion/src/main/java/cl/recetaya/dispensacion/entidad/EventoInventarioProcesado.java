package cl.recetaya.dispensacion.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evento_inventario_procesado")
public class EventoInventarioProcesado {
    @Id
    @Column(name = "evento_id", nullable = false, updatable = false)
    private UUID eventoId;

    @Column(name = "solicitud_id", nullable = false, updatable = false)
    private UUID solicitudId;

    @Column(name = "correlacion_id", nullable = false, updatable = false)
    private UUID correlacionId;

    @Column(name = "receta_id", nullable = false, updatable = false)
    private UUID recetaId;

    @Column(name = "fecha_procesamiento", nullable = false, updatable = false)
    private Instant fechaProcesamiento;

    protected EventoInventarioProcesado() {}

    public EventoInventarioProcesado(UUID eventoId, UUID solicitudId, UUID correlacionId,
                                     UUID recetaId, Instant fechaProcesamiento) {
        this.eventoId = eventoId;
        this.solicitudId = solicitudId;
        this.correlacionId = correlacionId;
        this.recetaId = recetaId;
        this.fechaProcesamiento = fechaProcesamiento;
    }
}
