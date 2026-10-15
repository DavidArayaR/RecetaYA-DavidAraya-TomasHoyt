package cl.recetaya.recetas.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "receta_detalle")
public class RecetaDetalle {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @Column(name = "medicamento_id", nullable = false)
    private UUID medicamentoId;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    protected RecetaDetalle() { }

    public RecetaDetalle(Receta receta, UUID medicamentoId, int cantidad) {
        this.id = UUID.randomUUID();
        this.receta = receta;
        this.medicamentoId = medicamentoId;
        this.cantidad = cantidad;
    }

    public void cambiarCantidad(int cantidad) { this.cantidad = cantidad; }

    public UUID getId() { return id; }
    public UUID getMedicamentoId() { return medicamentoId; }
    public int getCantidad() { return cantidad; }
}
