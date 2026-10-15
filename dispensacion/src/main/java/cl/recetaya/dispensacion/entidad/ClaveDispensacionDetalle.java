package cl.recetaya.dispensacion.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClaveDispensacionDetalle implements Serializable {
    @Column(name = "dispensacion_id", nullable = false)
    private UUID dispensacionId;

    @Column(name = "medicamento_id", nullable = false)
    private UUID medicamentoId;

    protected ClaveDispensacionDetalle() {}

    public ClaveDispensacionDetalle(UUID dispensacionId, UUID medicamentoId) {
        this.dispensacionId = dispensacionId;
        this.medicamentoId = medicamentoId;
    }

    public UUID getMedicamentoId() { return medicamentoId; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof ClaveDispensacionDetalle otra)) return false;
        return Objects.equals(dispensacionId, otra.dispensacionId)
                && Objects.equals(medicamentoId, otra.medicamentoId);
    }

    @Override
    public int hashCode() { return Objects.hash(dispensacionId, medicamentoId); }
}
