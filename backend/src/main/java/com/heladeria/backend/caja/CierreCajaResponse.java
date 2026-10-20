package com.heladeria.backend.caja;

import com.heladeria.backend.reporte.TotalPorMetodoPagoResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CierreCajaResponse(
        Long id,
        OffsetDateTime abiertaEn,
        OffsetDateTime cerradaEn,
        BigDecimal montoApertura,
        BigDecimal montoCierreEsperado,
        BigDecimal montoCierreContado,
        BigDecimal diferencia,
        BigDecimal totalVentas,
        String usuarioApertura,
        String usuarioCierre,
        String observacion,
        List<TotalPorMetodoPagoResponse> totalesPorMetodoPago,
        BigDecimal totalIngresos,
        BigDecimal totalRetiros,
        BigDecimal totalGastos
) {
}
