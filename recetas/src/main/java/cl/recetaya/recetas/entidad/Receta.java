package cl.recetaya.recetas.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "receta")
public class Receta {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "medico_usuario_id", nullable = false)
    private UUID medicoUsuarioId;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 32)
    private EstadoReceta estado;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecetaDetalle> detalles = new ArrayList<>();

    protected Receta() { }

    public Receta(Paciente paciente, UUID medicoUsuarioId, UUID sucursalId, LocalDateTime fechaEmision) {
        this.id = UUID.randomUUID();
        this.paciente = paciente;
        this.medicoUsuarioId = medicoUsuarioId;
        this.sucursalId = sucursalId;
        this.fechaEmision = fechaEmision;
        this.estado = EstadoReceta.ACEPTADA_PENDIENTE_RESERVA;
    }

    public void actualizar(Paciente paciente, UUID sucursalId) {
        this.paciente = paciente;
        this.sucursalId = sucursalId;
    }

    public void anular() { this.estado = EstadoReceta.ANULADA; }

    public void agregarDetalle(UUID medicamentoId, int cantidad) {
        detalles.add(new RecetaDetalle(this, medicamentoId, cantidad));
    }

    public UUID getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public UUID getMedicoUsuarioId() { return medicoUsuarioId; }
    public UUID getSucursalId() { return sucursalId; }
    public EstadoReceta getEstado() { return estado; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public List<RecetaDetalle> getDetalles() { return detalles; }
}
