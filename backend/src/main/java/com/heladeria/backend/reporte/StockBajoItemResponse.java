package com.heladeria.backend.reporte;

import java.math.BigDecimal;

public record StockBajoItemResponse(Long id, String nombre, BigDecimal stockActual, BigDecimal stockMinimo, String unidadMedida) {
}
