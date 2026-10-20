package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record CategoriaRankingResponse(
        String categoria,
        BigDecimal cantidadVendida,
        BigDecimal ingresos,
        BigDecimal ganancia
) {
}
