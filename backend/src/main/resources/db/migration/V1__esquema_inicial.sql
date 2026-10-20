-- Productos: catálogo + stock actual en la misma tabla (v1, simple)
CREATE TABLE productos (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    categoria       VARCHAR(60),
    costo_unitario  NUMERIC(12, 2) NOT NULL CHECK (costo_unitario >= 0),
    precio_venta    NUMERIC(12, 2) NOT NULL CHECK (precio_venta >= 0),
    unidad_medida   VARCHAR(20) NOT NULL DEFAULT 'unidad',
    stock_actual    NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Movimientos de inventario: auditoría de por qué cambió el stock
CREATE TABLE movimientos_inventario (
    id              BIGSERIAL PRIMARY KEY,
    producto_id     BIGINT NOT NULL REFERENCES productos(id),
    tipo            VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    cantidad        NUMERIC(12, 2) NOT NULL,
    motivo          VARCHAR(200),
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimientos_producto ON movimientos_inventario(producto_id);

-- Ventas: cabecera
CREATE TABLE ventas (
    id              BIGSERIAL PRIMARY KEY,
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now(),
    total           NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    metodo_pago     VARCHAR(30) NOT NULL DEFAULT 'EFECTIVO'
);

-- Ítems de venta: precio y costo "congelados" al momento de vender,
-- para que reportes históricos de ganancia no cambien si luego se edita el producto
CREATE TABLE venta_items (
    id                      BIGSERIAL PRIMARY KEY,
    venta_id                BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id             BIGINT NOT NULL REFERENCES productos(id),
    cantidad                NUMERIC(12, 2) NOT NULL CHECK (cantidad > 0),
    precio_unitario_momento NUMERIC(12, 2) NOT NULL CHECK (precio_unitario_momento >= 0),
    costo_unitario_momento  NUMERIC(12, 2) NOT NULL CHECK (costo_unitario_momento >= 0)
);

CREATE INDEX idx_venta_items_venta ON venta_items(venta_id);
CREATE INDEX idx_venta_items_producto ON venta_items(producto_id);
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
