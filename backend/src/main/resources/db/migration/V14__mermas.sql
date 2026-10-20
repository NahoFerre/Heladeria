CREATE TABLE mermas (
    id                      BIGSERIAL PRIMARY KEY,
    insumo_id               BIGINT REFERENCES insumos(id),
    producto_id             BIGINT REFERENCES productos(id),
    nombre_momento          VARCHAR(120) NOT NULL,
    cantidad                NUMERIC(12, 4) NOT NULL CHECK (cantidad > 0),
    costo_unitario          NUMERIC(12, 4) NOT NULL CHECK (costo_unitario >= 0),
    costo_total             NUMERIC(12, 2) NOT NULL CHECK (costo_total >= 0),
    motivo                  VARCHAR(30) NOT NULL,
    observacion             VARCHAR(300),
    usuario_id              BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_nombre_momento  VARCHAR(80),
    fecha                   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (
        (insumo_id IS NOT NULL AND producto_id IS NULL) OR
        (insumo_id IS NULL AND producto_id IS NOT NULL)
    )
);

CREATE INDEX idx_mermas_fecha ON mermas(fecha DESC);
