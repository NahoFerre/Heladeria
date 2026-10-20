package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record ProductoRankingResponse(
        Long productoId,
        String nombre,
        BigDecimal cantidadVendida,
        BigDecimal ingresos,
        BigDecimal ganancia
) {
}
