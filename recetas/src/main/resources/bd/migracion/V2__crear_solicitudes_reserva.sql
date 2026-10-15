ALTER TABLE receta ADD COLUMN codigo_rechazo VARCHAR(64);
ALTER TABLE receta DROP CONSTRAINT ck_receta_estado;
ALTER TABLE receta ADD CONSTRAINT ck_receta_estado
    CHECK (estado IN ('ACEPTADA_PENDIENTE_RESERVA', 'RESERVADA', 'RESERVA_RECHAZADA', 'ANULADA'));

CREATE TABLE solicitud_reserva (
    id UUID PRIMARY KEY,
    receta_id UUID NOT NULL UNIQUE,
    mensaje_id UUID NOT NULL UNIQUE,
    correlacion_id UUID NOT NULL UNIQUE,
    cuerpo_json CLOB NOT NULL,
    estado_publicacion VARCHAR(16) NOT NULL,
    intentos INTEGER NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP(6) NOT NULL,
    fecha_proximo_intento TIMESTAMP(6) NOT NULL,
    fecha_publicacion TIMESTAMP(6),
    error_publicacion VARCHAR(160),
    CONSTRAINT fk_solicitud_reserva_receta FOREIGN KEY (receta_id) REFERENCES receta(id),
    CONSTRAINT ck_solicitud_reserva_estado CHECK (estado_publicacion IN ('PENDIENTE', 'PUBLICADO', 'FALLIDO', 'CANCELADO')),
    CONSTRAINT ck_solicitud_reserva_intentos CHECK (intentos >= 0)
);

CREATE INDEX idx_solicitud_reserva_pendiente ON solicitud_reserva(estado_publicacion, fecha_proximo_intento);
