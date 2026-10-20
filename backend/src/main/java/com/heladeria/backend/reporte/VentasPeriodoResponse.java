package com.heladeria.backend.reporte;

import java.math.BigDecimal;

// variacionPorcentaje: null cuando el período anterior no tuvo ventas y el actual tampoco permite calcular un %
public record VentasPeriodoResponse(long cantidadVentas, BigDecimal totalVendido, BigDecimal variacionPorcentaje) {
}
