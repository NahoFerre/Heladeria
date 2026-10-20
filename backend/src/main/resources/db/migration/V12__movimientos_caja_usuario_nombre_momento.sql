ALTER TABLE movimientos_caja ADD COLUMN usuario_nombre_momento VARCHAR(80);
UPDATE movimientos_caja m SET usuario_nombre_momento = u.username FROM usuarios u WHERE m.usuario_id = u.id;

ALTER TABLE movimientos_caja DROP CONSTRAINT movimientos_caja_usuario_id_fkey;
ALTER TABLE movimientos_caja ADD CONSTRAINT movimientos_caja_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL;
