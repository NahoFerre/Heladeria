CREATE TABLE producto_variantes (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    nombre          VARCHAR(80) NOT NULL,
    precio          NUMERIC(12, 2) NOT NULL CHECK (precio >= 0),
    costo           NUMERIC(12, 2) NOT NULL CHECK (costo >= 0),
    sku             VARCHAR(50),
    stock_actual    NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_variantes_producto ON producto_variantes(producto_id);

CREATE TABLE grupos_adicionales (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    nombre          VARCHAR(80) NOT NULL,
    min_seleccion   INT NOT NULL DEFAULT 0 CHECK (min_seleccion >= 0),
    max_seleccion   INT NOT NULL DEFAULT 1 CHECK (max_seleccion >= 1)
);

CREATE INDEX idx_grupos_producto ON grupos_adicionales(producto_id);

CREATE TABLE adicionales (
    id                  BIGSERIAL PRIMARY KEY,
    grupo_id            BIGINT NOT NULL REFERENCES grupos_adicionales(id) ON DELETE CASCADE,
    nombre              VARCHAR(80) NOT NULL,
    precio_adicional    NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (precio_adicional >= 0),
    costo_adicional     NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (costo_adicional >= 0),
    controla_stock      BOOLEAN NOT NULL DEFAULT FALSE,
    stock_actual        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    stock_minimo        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    activo              BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_adicionales_grupo ON adicionales(grupo_id);

ALTER TABLE venta_items ADD COLUMN variante_id BIGINT REFERENCES producto_variantes(id) ON DELETE SET NULL;
ALTER TABLE venta_items ADD COLUMN variante_nombre_momento VARCHAR(80);

CREATE TABLE venta_item_adicionales (
    id                          BIGSERIAL PRIMARY KEY,
    venta_item_id               BIGINT NOT NULL REFERENCES venta_items(id) ON DELETE CASCADE,
    adicional_id                BIGINT REFERENCES adicionales(id) ON DELETE SET NULL,
    nombre_momento              VARCHAR(80) NOT NULL,
    precio_adicional_momento    NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_venta_item_adicionales_item ON venta_item_adicionales(venta_item_id);
