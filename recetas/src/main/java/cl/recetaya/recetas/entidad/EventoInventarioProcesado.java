package cl.recetaya.recetas.entidad;

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
    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "correlacion_id", nullable = false)
    private UUID correlacionId;

    @Column(name = "fecha_procesamiento", nullable = false)
    private Instant fechaProcesamiento;

    protected EventoInventarioProcesado() { }

    public EventoInventarioProcesado(UUID eventoId, UUID correlacionId, Instant fechaProcesamiento) {
        this.eventoId = eventoId;
        this.correlacionId = correlacionId;
        this.fechaProcesamiento = fechaProcesamiento;
    }
}
