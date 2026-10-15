package cl.recetaya.dispensacion.entidad;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "dispensacion_detalle")
public class DispensacionDetalle {
    @EmbeddedId
    private ClaveDispensacionDetalle id;

    @MapsId("dispensacionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispensacion_id", nullable = false)
    private Dispensacion dispensacion;

    @jakarta.persistence.Column(name = "cantidad", nullable = false)
    private int cantidad;

    protected DispensacionDetalle() {}

    public DispensacionDetalle(Dispensacion dispensacion, java.util.UUID medicamentoId, int cantidad) {
        this.dispensacion = dispensacion;
        this.id = new ClaveDispensacionDetalle(dispensacion.getId(), medicamentoId);
        this.cantidad = cantidad;
    }

    public java.util.UUID getMedicamentoId() { return id == null ? null : id.getMedicamentoId(); }
    public int getCantidad() { return cantidad; }
}
