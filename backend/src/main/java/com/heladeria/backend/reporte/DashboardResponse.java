package com.heladeria.backend.reporte;

import com.heladeria.backend.caja.CajaActualResponse;

// cajaActual: null si no hay ninguna caja abierta en este momento
public record DashboardResponse(
        DashboardVentasResponse ventas,
        DashboardProductosResponse productos,
        CajaActualResponse cajaActual,
        DashboardInventarioResponse inventario
) {
}
