CREATE TABLE movimientos_caja (
    id          BIGSERIAL PRIMARY KEY,
    caja_id     BIGINT NOT NULL REFERENCES cajas(id),
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('RETIRO', 'INGRESO', 'GASTO')),
    monto       NUMERIC(12, 2) NOT NULL CHECK (monto > 0),
    motivo      VARCHAR(300) NOT NULL,
    usuario_id  BIGINT REFERENCES usuarios(id),
    fecha       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimientos_caja_caja ON movimientos_caja(caja_id);
