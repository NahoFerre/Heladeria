package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record ReporteGananciaResponse(
        BigDecimal totalVentas,
        BigDecimal totalCosto,
        BigDecimal ganancia,
        long cantidadItemsVendidos,
        long cantidadVentas,
        BigDecimal costoMerma,
        BigDecimal gastosOperativos,
        BigDecimal gananciaOperativa,
        BigDecimal margenPorcentaje,
        BigDecimal ticketPromedio
) {
}
