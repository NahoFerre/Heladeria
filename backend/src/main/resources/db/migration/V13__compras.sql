CREATE TABLE compras (
    id                      BIGSERIAL PRIMARY KEY,
    proveedor               VARCHAR(120),
    numero_factura          VARCHAR(50),
    metodo_pago             VARCHAR(30),
    observaciones           VARCHAR(300),
    total                   NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    usuario_id              BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_nombre_momento  VARCHAR(80),
    fecha                   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE compra_items (
    id              BIGSERIAL PRIMARY KEY,
    compra_id       BIGINT NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    insumo_id       BIGINT REFERENCES insumos(id),
    producto_id     BIGINT REFERENCES productos(id),
    nombre_momento  VARCHAR(120) NOT NULL,
    cantidad        NUMERIC(12, 4) NOT NULL CHECK (cantidad > 0),
    costo_unitario  NUMERIC(12, 4) NOT NULL CHECK (costo_unitario >= 0),
    subtotal        NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    CHECK (
        (insumo_id IS NOT NULL AND producto_id IS NULL) OR
        (insumo_id IS NULL AND producto_id IS NOT NULL)
    )
);

CREATE INDEX idx_compra_items_compra ON compra_items(compra_id);
CREATE INDEX idx_compras_fecha ON compras(fecha DESC);
