CREATE TABLE paciente (
    id UUID PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL
);

CREATE TABLE receta (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medico_usuario_id UUID NOT NULL,
    sucursal_id UUID NOT NULL,
    estado VARCHAR(32) NOT NULL,
    fecha_emision TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_receta_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id),
    CONSTRAINT ck_receta_estado CHECK (estado IN ('ACEPTADA_PENDIENTE_RESERVA', 'ANULADA'))
);

CREATE TABLE receta_detalle (
    id UUID PRIMARY KEY,
    receta_id UUID NOT NULL,
    medicamento_id UUID NOT NULL,
    cantidad INTEGER NOT NULL,
    CONSTRAINT fk_detalle_receta FOREIGN KEY (receta_id) REFERENCES receta(id),
    CONSTRAINT uq_receta_medicamento UNIQUE (receta_id, medicamento_id),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_receta_medico_fecha ON receta(medico_usuario_id, fecha_emision);
CREATE INDEX idx_receta_paciente_fecha ON receta(paciente_id, fecha_emision);
CREATE INDEX idx_receta_estado ON receta(estado);
CREATE INDEX idx_detalle_receta ON receta_detalle(receta_id);
