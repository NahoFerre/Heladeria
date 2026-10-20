ALTER TABLE venta_item_adicionales ADD COLUMN costo_adicional_momento NUMERIC(12, 4) NOT NULL DEFAULT 0;

-- Backfill con el costo ACTUAL del adicional (no hay forma de recuperar el costo histórico real
-- de ventas pasadas); las filas cuyo adicional ya fue eliminado quedan en 0 (aproximación documentada).
UPDATE venta_item_adicionales via
SET costo_adicional_momento = a.costo_adicional
FROM adicionales a
WHERE via.adicional_id = a.id;
