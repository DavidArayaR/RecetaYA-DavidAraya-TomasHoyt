package cl.recetaya.recetas.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitud_reserva")
public class SolicitudReserva {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receta_id", nullable = false, unique = true)
    private Receta receta;

    @Column(name = "mensaje_id", nullable = false, unique = true)
    private UUID mensajeId;

    @Column(name = "correlacion_id", nullable = false, unique = true)
    private UUID correlacionId;

    @Column(name = "cuerpo_json", nullable = false, columnDefinition = "CLOB")
    private String cuerpoJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false, length = 16)
    private EstadoPublicacionReserva estadoPublicacion;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_proximo_intento", nullable = false)
    private Instant fechaProximoIntento;

    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    @Column(name = "error_publicacion", length = 160)
    private String errorPublicacion;

    protected SolicitudReserva() { }

    public SolicitudReserva(Receta receta, UUID mensajeId, UUID correlacionId, String cuerpoJson, Instant ahora) {
        this.id = UUID.randomUUID();
        this.receta = receta;
        this.mensajeId = mensajeId;
        this.correlacionId = correlacionId;
        this.cuerpoJson = cuerpoJson;
        this.estadoPublicacion = EstadoPublicacionReserva.PENDIENTE;
        this.intentos = 0;
        this.fechaCreacion = ahora;
        this.fechaProximoIntento = ahora;
    }

    public void actualizarCuerpoPendiente(String cuerpoJson) {
        if (estadoPublicacion != EstadoPublicacionReserva.PENDIENTE) {
            throw new IllegalStateException("La solicitud de reserva ya no admite cambios.");
        }
        this.cuerpoJson = cuerpoJson;
    }

    public void marcarPublicado(Instant ahora) {
        estadoPublicacion = EstadoPublicacionReserva.PUBLICADO;
        intentos++;
        fechaPublicacion = ahora;
        errorPublicacion = null;
    }

    public void registrarErrorPublicacion(Instant ahora, int maximoIntentos) {
        intentos++;
        errorPublicacion = "No se pudo publicar la solicitud en la cola de Inventario.";
        if (intentos >= maximoIntentos) {
            estadoPublicacion = EstadoPublicacionReserva.FALLIDO;
        } else {
            long esperaSegundos = Math.min(300, 1L << Math.min(intentos, 8));
            fechaProximoIntento = ahora.plusSeconds(esperaSegundos);
        }
    }

    public void cancelarPendiente() {
        if (estadoPublicacion != EstadoPublicacionReserva.PENDIENTE) {
            throw new IllegalStateException("No se puede anular una receta cuya solicitud ya se publicó.");
        }
        estadoPublicacion = EstadoPublicacionReserva.CANCELADO;
    }

    public UUID getId() { return id; }
    public Receta getReceta() { return receta; }
    public UUID getMensajeId() { return mensajeId; }
    public UUID getCorrelacionId() { return correlacionId; }
    public String getCuerpoJson() { return cuerpoJson; }
    public EstadoPublicacionReserva getEstadoPublicacion() { return estadoPublicacion; }
    public int getIntentos() { return intentos; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public Instant getFechaProximoIntento() { return fechaProximoIntento; }
    public Instant getFechaPublicacion() { return fechaPublicacion; }
    public String getErrorPublicacion() { return errorPublicacion; }
}
