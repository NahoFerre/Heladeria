package com.heladeria.backend.reporte;

public record DashboardVentasResponse(
        VentasPeriodoResponse hoy,
        VentasPeriodoResponse semana,
        VentasPeriodoResponse mes
) {
}
