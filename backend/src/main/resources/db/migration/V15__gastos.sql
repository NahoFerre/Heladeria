CREATE TABLE gastos (
    id                      BIGSERIAL PRIMARY KEY,
    categoria               VARCHAR(80),
    descripcion             VARCHAR(300) NOT NULL,
    monto                   NUMERIC(12, 2) NOT NULL CHECK (monto > 0),
    metodo_pago             VARCHAR(30),
    observacion             VARCHAR(300),
    usuario_id              BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_nombre_momento  VARCHAR(80),
    fecha                   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_gastos_fecha ON gastos(fecha DESC);
