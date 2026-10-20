package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record VentaPorUsuarioResponse(String usuario, long cantidadVentas, BigDecimal totalVendido) {
}
