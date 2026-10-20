package com.heladeria.backend.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface VentaItemAdicionalRepository extends JpaRepository<VentaItemAdicional, Long> {

    @Query("""
            select coalesce(sum(via.costoAdicionalMomento * via.ventaItem.cantidad), 0)
            from VentaItemAdicional via
            where via.ventaItem.venta.fecha between :desde and :hasta
              and via.ventaItem.venta.estado = com.heladeria.backend.venta.EstadoVenta.CONFIRMADA
            """)
    BigDecimal costoAdicionalesEnPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);
}
