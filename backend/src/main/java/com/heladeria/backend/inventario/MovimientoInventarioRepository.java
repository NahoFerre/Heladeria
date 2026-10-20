package com.heladeria.backend.inventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId);

    @Query("""
            select new com.heladeria.backend.reporte.MovimientoInventarioReporteResponse(
                m.id, m.fecha, p.nombre, m.tipo, m.cantidad, m.motivo
            )
            from MovimientoInventario m join m.producto p
            where m.fecha between :desde and :hasta
            and (:productoId is null or p.id = :productoId)
            order by m.fecha desc
            """)
    List<com.heladeria.backend.reporte.MovimientoInventarioReporteResponse> buscarFiltrados(
            @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta, @Param("productoId") Long productoId);
}
