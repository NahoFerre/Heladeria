package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record ResumenVentasResponse(long cantidadVentas, BigDecimal totalVendido) {
}
