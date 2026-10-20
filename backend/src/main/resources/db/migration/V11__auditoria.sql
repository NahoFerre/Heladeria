CREATE TABLE registros_auditoria (
    id                      BIGSERIAL PRIMARY KEY,
    usuario_id              BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_nombre_momento  VARCHAR(80),
    accion                  VARCHAR(30) NOT NULL,
    entidad                 VARCHAR(40),
    entidad_id              BIGINT,
    detalle                 VARCHAR(500),
    ip                      VARCHAR(45),
    fecha                   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_fecha ON registros_auditoria(fecha DESC);
CREATE INDEX idx_auditoria_usuario ON registros_auditoria(usuario_id);
