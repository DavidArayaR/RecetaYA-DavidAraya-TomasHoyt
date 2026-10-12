CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    correo_electronico VARCHAR(254) NOT NULL UNIQUE,
    hash_contrasena VARCHAR(255) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL
);

CREATE TABLE rol (
    id UUID PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL
);

CREATE TABLE usuario_rol (
    usuario_id UUID NOT NULL,
    rol_id UUID NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (rol_id) REFERENCES rol(id) ON DELETE CASCADE
);

INSERT INTO rol (id, codigo, nombre) VALUES
    (RANDOM_UUID(), 'MEDICO', 'Médico'),
    (RANDOM_UUID(), 'FARMACEUTICO', 'Farmacéutico');
