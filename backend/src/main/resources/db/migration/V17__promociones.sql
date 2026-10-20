CREATE TABLE promociones (
    id               BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(120) NOT NULL,
    tipo             VARCHAR(30) NOT NULL,
    valor            NUMERIC(12, 2) NOT NULL CHECK (valor >= 0),
    producto_id      BIGINT REFERENCES productos(id) ON DELETE CASCADE,
    categoria        VARCHAR(80),
    fecha_inicio     DATE,
    fecha_fin        DATE,
    cantidad_minima  NUMERIC(12, 2) NOT NULL DEFAULT 1 CHECK (cantidad_minima > 0),
    activo           BOOLEAN NOT NULL DEFAULT true,
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_promociones_activo ON promociones(activo);

-- Se congela qué promoción aplicó (si aplicó alguna) al momento de la venta, igual que el resto de los
-- campos "momento" del sistema: si la promoción se edita o desactiva después, el historial no cambia.
ALTER TABLE venta_items ADD COLUMN promocion_id BIGINT REFERENCES promociones(id) ON DELETE SET NULL;
ALTER TABLE venta_items ADD COLUMN promocion_nombre_momento VARCHAR(120);
