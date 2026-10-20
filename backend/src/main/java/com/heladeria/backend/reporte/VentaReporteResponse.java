package com.heladeria.backend.reporte;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VentaReporteResponse(
        Long id,
        OffsetDateTime fecha,
        String usuario,
        String metodoPago,
        BigDecimal total,
        Long cajaId
) {
}
