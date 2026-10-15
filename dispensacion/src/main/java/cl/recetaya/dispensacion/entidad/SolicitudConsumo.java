package cl.recetaya.dispensacion.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitud_consumo")
public class SolicitudConsumo {
    @Id
    @Column(name = "mensaje_id", nullable = false, updatable = false)
    private UUID mensajeId;

    @Column(name = "correlacion_id", nullable = false, updatable = false)
    private UUID correlacionId;

    @Column(name = "receta_id", nullable = false, updatable = false, unique = true)
    private UUID recetaId;

    @Column(name = "contenido", nullable = false, columnDefinition = "CLOB")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 16)
    private EstadoSolicitudConsumo estado;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "proximo_intento", nullable = false)
    private Instant proximoIntento;

    @Column(name = "ultimo_error", length = 500)
    private String ultimoError;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    protected SolicitudConsumo() {}

    public SolicitudConsumo(UUID mensajeId, UUID correlacionId, UUID recetaId,
                            String contenido, Instant ahora) {
        this.mensajeId = mensajeId;
        this.correlacionId = correlacionId;
        this.recetaId = recetaId;
        this.contenido = contenido;
        this.estado = EstadoSolicitudConsumo.PENDIENTE;
        this.proximoIntento = ahora;
        this.fechaCreacion = ahora;
    }

    public void registrarFallo(String detalle, Instant siguienteIntento, boolean definitivo) {
        intentos++;
        ultimoError = detalle == null ? "Error de publicación" : detalle.substring(0, Math.min(detalle.length(), 500));
        estado = definitivo ? EstadoSolicitudConsumo.FALLIDA : EstadoSolicitudConsumo.PENDIENTE;
        proximoIntento = siguienteIntento;
    }

    public void marcarPublicada() { estado = EstadoSolicitudConsumo.PUBLICADA; ultimoError = null; }
    public UUID getMensajeId() { return mensajeId; }
    public UUID getCorrelacionId() { return correlacionId; }
    public UUID getRecetaId() { return recetaId; }
    public String getContenido() { return contenido; }
    public EstadoSolicitudConsumo getEstado() { return estado; }
    public int getIntentos() { return intentos; }
    public Instant getProximoIntento() { return proximoIntento; }
}
