CREATE TABLE cajas (
    id                      BIGSERIAL PRIMARY KEY,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'ABIERTA' CHECK (estado IN ('ABIERTA', 'CERRADA')),
    monto_apertura          NUMERIC(12, 2) NOT NULL CHECK (monto_apertura >= 0),
    monto_cierre_esperado   NUMERIC(12, 2),
    monto_cierre_contado    NUMERIC(12, 2),
    diferencia              NUMERIC(12, 2),
    total_ventas            NUMERIC(12, 2),
    observacion             VARCHAR(300),
    usuario_apertura_id     BIGINT REFERENCES usuarios(id),
    usuario_cierre_id       BIGINT REFERENCES usuarios(id),
    abierta_en              TIMESTAMPTZ NOT NULL DEFAULT now(),
    cerrada_en              TIMESTAMPTZ
);

-- Garantiza a nivel de base de datos que solo puede haber una caja abierta a la vez
CREATE UNIQUE INDEX uq_una_caja_abierta ON cajas (estado) WHERE estado = 'ABIERTA';

ALTER TABLE ventas ADD COLUMN caja_id BIGINT REFERENCES cajas(id);

CREATE INDEX idx_ventas_caja ON ventas(caja_id);
