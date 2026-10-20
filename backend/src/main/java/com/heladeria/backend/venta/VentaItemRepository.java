package com.heladeria.backend.venta;

import com.heladeria.backend.reporte.CategoriaRankingResponse;
import com.heladeria.backend.reporte.ProductoRankingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface VentaItemRepository extends JpaRepository<VentaItem, Long> {

    @Query("""
            select coalesce(sum(vi.cantidad * vi.costoUnitarioMomento), 0)
            from VentaItem vi
            where vi.venta.fecha between :desde and :hasta and vi.venta.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            """)
    BigDecimal costoProductosEnPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("""
            select count(vi)
            from VentaItem vi
            where vi.venta.fecha between :desde and :hasta and vi.venta.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            """)
    long contarItemsEnPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("""
            select new com.heladeria.backend.reporte.ProductoRankingResponse(
                p.id, p.nombre,
                sum(vi.cantidad),
                sum(vi.cantidad * vi.precioUnitarioMomento),
                sum(vi.cantidad * (vi.precioUnitarioMomento - vi.costoUnitarioMomento))
            )
            from VentaItem vi join vi.producto p
            where vi.venta.fecha between :desde and :hasta and vi.venta.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            group by p.id, p.nombre
            order by sum(vi.cantidad * (vi.precioUnitarioMomento - vi.costoUnitarioMomento)) desc
            """)
    List<ProductoRankingResponse> rankingProductos(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("""
            select new com.heladeria.backend.reporte.CategoriaRankingResponse(
                coalesce(p.categoria, 'Sin categoría'),
                sum(vi.cantidad),
                sum(vi.cantidad * vi.precioUnitarioMomento),
                sum(vi.cantidad * (vi.precioUnitarioMomento - vi.costoUnitarioMomento))
            )
            from VentaItem vi join vi.producto p
            where vi.venta.fecha between :desde and :hasta and vi.venta.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            group by p.categoria
            order by sum(vi.cantidad * (vi.precioUnitarioMomento - vi.costoUnitarioMomento)) desc
            """)
    List<CategoriaRankingResponse> rankingCategorias(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);
}
