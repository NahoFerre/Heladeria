package com.heladeria.backend.reporte;

// Los cuatro campos, sobre las ventas del mes en curso. Null si todavía no hubo ninguna venta este mes.
public record DashboardProductosResponse(
        ProductoRankingResponse masVendido,
        ProductoRankingResponse mayorGanancia,
        ProductoRankingResponse menorRotacion,
        ProductoRankingResponse mayorMargen
) {
}
