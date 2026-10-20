CREATE TABLE insumos (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    unidad_medida   VARCHAR(20) NOT NULL DEFAULT 'UNIDAD' CHECK (unidad_medida IN ('UNIDAD', 'GRAMO', 'KILOGRAMO', 'MILILITRO', 'LITRO')),
    costo_unitario  NUMERIC(12, 4) NOT NULL CHECK (costo_unitario >= 0),
    stock_actual    NUMERIC(12, 4) NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo    NUMERIC(12, 4) NOT NULL DEFAULT 0,
    proveedor       VARCHAR(120),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE receta_items (
    id          BIGSERIAL PRIMARY KEY,
    producto_id BIGINT REFERENCES productos(id) ON DELETE CASCADE,
    variante_id BIGINT REFERENCES producto_variantes(id) ON DELETE CASCADE,
    insumo_id   BIGINT NOT NULL REFERENCES insumos(id),
    cantidad    NUMERIC(12, 4) NOT NULL CHECK (cantidad > 0),
    CHECK (
        (producto_id IS NOT NULL AND variante_id IS NULL) OR
        (producto_id IS NULL AND variante_id IS NOT NULL)
    )
);

CREATE INDEX idx_receta_producto ON receta_items(producto_id);
CREATE INDEX idx_receta_variante ON receta_items(variante_id);
CREATE INDEX idx_receta_insumo ON receta_items(insumo_id);
