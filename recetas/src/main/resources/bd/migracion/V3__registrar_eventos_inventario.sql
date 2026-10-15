CREATE TABLE evento_inventario_procesado (
    evento_id UUID PRIMARY KEY,
    correlacion_id UUID NOT NULL,
    fecha_procesamiento TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_evento_inventario_correlacion ON evento_inventario_procesado(correlacion_id);
