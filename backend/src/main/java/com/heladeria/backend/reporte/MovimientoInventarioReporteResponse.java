package com.heladeria.backend.reporte;

import com.heladeria.backend.inventario.TipoMovimiento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MovimientoInventarioReporteResponse(
        Long id,
        OffsetDateTime fecha,
        String producto,
        TipoMovimiento tipo,
        BigDecimal cantidad,
        String motivo
) {
}
