package cl.recetaya.dispensacion.entidad;

import cl.recetaya.dispensacion.integracion.DetalleRecetaValidada;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dispensacion", uniqueConstraints = {
        @UniqueConstraint(name = "uq_dispensacion_receta", columnNames = "receta_id"),
        @UniqueConstraint(name = "uq_dispensacion_reserva", columnNames = "reserva_id")
})
public class Dispensacion {
    @Id
    private UUID id;

    @Column(name = "receta_id", nullable = false, updatable = false)
    private UUID recetaId;

    @Column(name = "reserva_id", nullable = false, updatable = false)
    private UUID reservaId;

    @Column(name = "farmaceutico_usuario_id", nullable = false, updatable = false)
    private UUID farmaceuticoUsuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoDispensacion estado;

    @Column(name = "codigo_rechazo", length = 64)
    private String codigoRechazo;

    @Column(name = "fecha_dispensacion", nullable = false, updatable = false)
    private Instant fechaDispensacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(mappedBy = "dispensacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispensacionDetalle> detalles = new ArrayList<>();

    protected Dispensacion() {}

    public Dispensacion(UUID recetaId, UUID reservaId, UUID farmaceuticoUsuarioId,
                        List<DetalleRecetaValidada> productos, Instant ahora) {
        this.id = UUID.randomUUID();
        this.recetaId = recetaId;
        this.reservaId = reservaId;
        this.farmaceuticoUsuarioId = farmaceuticoUsuarioId;
        this.estado = EstadoDispensacion.PENDIENTE_CONSUMO;
        this.fechaDispensacion = ahora;
        this.fechaActualizacion = ahora;
        productos.forEach(producto -> detalles.add(new DispensacionDetalle(this,
                producto.medicamentoId(), producto.cantidad())));
    }

    public void completar(Instant ahora) {
        if (estado == EstadoDispensacion.COMPLETADA) return;
        if (estado != EstadoDispensacion.PENDIENTE_CONSUMO) {
            throw new IllegalStateException("La dispensación no puede completarse desde su estado actual.");
        }
        estado = EstadoDispensacion.COMPLETADA;
        fechaActualizacion = ahora;
    }

    public void rechazar(String codigoRechazo, Instant ahora) {
        if (estado == EstadoDispensacion.RECHAZADA) return;
        if (estado != EstadoDispensacion.PENDIENTE_CONSUMO) {
            throw new IllegalStateException("La dispensación no puede rechazarse desde su estado actual.");
        }
        estado = EstadoDispensacion.RECHAZADA;
        this.codigoRechazo = codigoRechazo;
        fechaActualizacion = ahora;
    }

    public UUID getId() { return id; }
    public UUID getRecetaId() { return recetaId; }
    public UUID getReservaId() { return reservaId; }
    public UUID getFarmaceuticoUsuarioId() { return farmaceuticoUsuarioId; }
    public EstadoDispensacion getEstado() { return estado; }
    public String getCodigoRechazo() { return codigoRechazo; }
    public Instant getFechaDispensacion() { return fechaDispensacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public long getVersion() { return version; }
    public List<DispensacionDetalle> getDetalles() { return List.copyOf(detalles); }
}
