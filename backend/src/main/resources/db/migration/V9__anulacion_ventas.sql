ALTER TABLE ventas ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA' CHECK (estado IN ('CONFIRMADA', 'ANULADA'));
ALTER TABLE ventas ADD COLUMN anulada_en TIMESTAMPTZ;
ALTER TABLE ventas ADD COLUMN anulada_por_id BIGINT REFERENCES usuarios(id);
ALTER TABLE ventas ADD COLUMN motivo_anulacion VARCHAR(500);

ALTER TABLE venta_item_adicionales ADD COLUMN cantidad_stock_descontada NUMERIC(12, 4);

CREATE TABLE venta_item_insumos (
    id                  BIGSERIAL PRIMARY KEY,
    venta_item_id       BIGINT NOT NULL REFERENCES venta_items(id) ON DELETE CASCADE,
    insumo_id           BIGINT NOT NULL REFERENCES insumos(id),
    nombre_momento      VARCHAR(120) NOT NULL,
    cantidad_consumida  NUMERIC(12, 4) NOT NULL
);

CREATE INDEX idx_venta_item_insumos_item ON venta_item_insumos(venta_item_id);
