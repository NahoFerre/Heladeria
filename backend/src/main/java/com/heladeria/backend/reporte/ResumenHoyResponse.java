package com.heladeria.backend.reporte;

import java.math.BigDecimal;

// Solo datos "seguros" para mostrar a cualquier rol (sin costos ni ganancia)
public record ResumenHoyResponse(long ventasHoy, BigDecimal totalVendidoHoy, int productosStockBajo) {
}
