CREATE TABLE dispensacion (
    id UUID PRIMARY KEY,
    receta_id UUID NOT NULL,
    reserva_id UUID NOT NULL,
    farmaceutico_usuario_id UUID NOT NULL,
    estado VARCHAR(32) NOT NULL,
    codigo_rechazo VARCHAR(64),
    fecha_dispensacion TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_dispensacion_receta UNIQUE (receta_id),
    CONSTRAINT uq_dispensacion_reserva UNIQUE (reserva_id),
    CONSTRAINT ck_dispensacion_estado CHECK (estado IN ('PENDIENTE_CONSUMO', 'COMPLETADA', 'RECHAZADA'))
);

CREATE TABLE dispensacion_detalle (
    dispensacion_id UUID NOT NULL,
    medicamento_id UUID NOT NULL,
    cantidad INTEGER NOT NULL,
    CONSTRAINT pk_dispensacion_detalle PRIMARY KEY (dispensacion_id, medicamento_id),
    CONSTRAINT fk_detalle_dispensacion FOREIGN KEY (dispensacion_id) REFERENCES dispensacion (id),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX ix_dispensacion_estado ON dispensacion (estado);
CREATE INDEX ix_detalle_medicamento ON dispensacion_detalle (medicamento_id);

CREATE TABLE solicitud_consumo (
    mensaje_id UUID PRIMARY KEY,
    correlacion_id UUID NOT NULL,
    receta_id UUID NOT NULL,
    contenido CLOB NOT NULL,
    estado VARCHAR(16) NOT NULL,
    intentos INTEGER NOT NULL DEFAULT 0,
    proximo_intento TIMESTAMP WITH TIME ZONE NOT NULL,
    ultimo_error VARCHAR(500),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_solicitud_consumo_receta UNIQUE (receta_id),
    CONSTRAINT ck_solicitud_consumo_estado CHECK (estado IN ('PENDIENTE', 'PUBLICADA', 'FALLIDA')),
    CONSTRAINT ck_solicitud_consumo_intentos CHECK (intentos >= 0)
);

CREATE INDEX ix_solicitud_consumo_pendiente ON solicitud_consumo (estado, proximo_intento);

CREATE TABLE evento_inventario_procesado (
    evento_id UUID PRIMARY KEY,
    solicitud_id UUID NOT NULL,
    correlacion_id UUID NOT NULL,
    receta_id UUID NOT NULL,
    fecha_procesamiento TIMESTAMP WITH TIME ZONE NOT NULL
);
