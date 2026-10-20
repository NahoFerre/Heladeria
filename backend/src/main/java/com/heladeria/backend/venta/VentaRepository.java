package com.heladeria.backend.venta;

import com.heladeria.backend.reporte.ResumenVentasResponse;
import com.heladeria.backend.reporte.TotalPorMetodoPagoResponse;
import com.heladeria.backend.reporte.VentaPorUsuarioResponse;
import com.heladeria.backend.reporte.VentaReporteResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findAllByOrderByFechaDesc();

    @Query("""
            select new com.heladeria.backend.reporte.ResumenVentasResponse(
                count(v), coalesce(sum(v.total), 0.0)
            )
            from Venta v
            where v.fecha between :desde and :hasta and v.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            """)
    ResumenVentasResponse resumenVentas(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("""
            select new com.heladeria.backend.reporte.VentaPorUsuarioResponse(
                u.username, count(v), coalesce(sum(v.total), 0.0)
            )
            from Venta v join v.usuario u
            where v.fecha between :desde and :hasta and v.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            group by u.username
            order by sum(v.total) desc
            """)
    List<VentaPorUsuarioResponse> ventasPorUsuario(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    @Query("""
            select new com.heladeria.backend.reporte.TotalPorMetodoPagoResponse(
                v.metodoPago, count(v), coalesce(sum(v.total), 0.0)
            )
            from Venta v
            where v.caja.id = :cajaId and v.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            group by v.metodoPago
            """)
    List<TotalPorMetodoPagoResponse> totalesPorMetodoPagoDeCaja(@Param("cajaId") Long cajaId);

    @Query("""
            select new com.heladeria.backend.reporte.TotalPorMetodoPagoResponse(
                v.metodoPago, count(v), coalesce(sum(v.total), 0.0)
            )
            from Venta v
            where v.fecha between :desde and :hasta and v.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            group by v.metodoPago
            order by sum(v.total) desc
            """)
    List<TotalPorMetodoPagoResponse> totalesPorMetodoPagoEnPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    // Reporte de ventas con filtros opcionales (todos null = solo el rango de fechas manda)
    @Query("""
            select new com.heladeria.backend.reporte.VentaReporteResponse(
                v.id, v.fecha, u.username, v.metodoPago, v.total, v.caja.id
            )
            from Venta v join v.usuario u
            where v.fecha between :desde and :hasta and v.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            and (:usuarioId is null or u.id = :usuarioId)
            and (:metodoPago is null or v.metodoPago = :metodoPago)
            and (:cajaId is null or v.caja.id = :cajaId)
            order by v.fecha desc
            """)
    List<VentaReporteResponse> buscarFiltradas(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta,
                                                @Param("usuarioId") Long usuarioId, @Param("metodoPago") String metodoPago,
                                                @Param("cajaId") Long cajaId);
}
