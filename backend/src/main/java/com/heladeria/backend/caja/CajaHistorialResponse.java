package com.heladeria.backend.caja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CajaHistorialResponse(
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
        BigDecimal totalIngresos,
        BigDecimal totalRetiros,
        BigDecimal totalGastos
) {
}
