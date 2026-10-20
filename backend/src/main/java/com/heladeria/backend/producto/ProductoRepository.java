package com.heladeria.backend.producto;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    // Bloqueo pesimista: evita que dos ventas simultáneas descuenten stock del mismo producto a la vez
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizarStock(@Param("id") Long id);

    @Query("select p from Producto p where p.activo = true and p.stockActual <= p.stockMinimo")
    List<Producto> buscarStockBajo();

    @Query("select distinct p.categoria from Producto p where p.categoria is not null and p.categoria <> '' order by p.categoria")
    List<String> buscarCategorias();

    // Activos que no tuvieron ni un solo ítem vendido (en venta confirmada) desde la fecha dada
    @Query("""
            select p from Producto p
            where p.activo = true
            and p.id not in (
                select vi.producto.id from VentaItem vi
                where vi.venta.fecha >= :desde and vi.venta.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            )
            order by p.nombre
            """)
    List<Producto> buscarSinMovimientoDesde(@Param("desde") OffsetDateTime desde);
}
