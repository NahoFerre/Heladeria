package com.heladeria.backend.reporte;

import java.util.List;

public record DashboardInventarioResponse(
        List<StockBajoItemResponse> stockBajoProductos,
        List<StockBajoItemResponse> stockBajoInsumos,
        List<ProductoSinMovimientoResponse> productosSinMovimiento
) {
}
