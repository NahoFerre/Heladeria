package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record TotalPorMetodoPagoResponse(String metodoPago, long cantidadVentas, BigDecimal totalVendido) {
}
