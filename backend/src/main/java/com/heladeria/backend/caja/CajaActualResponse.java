package com.heladeria.backend.caja;

import com.heladeria.backend.reporte.TotalPorMetodoPagoResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CajaActualResponse(
        Long id,
        OffsetDateTime abiertaEn,
        BigDecimal montoApertura,
        String usuarioApertura,
        BigDecimal totalEfectivoVendido,
        List<TotalPorMetodoPagoResponse> totalesPorMetodoPago,
        BigDecimal totalIngresos,
        BigDecimal totalRetiros,
        BigDecimal totalGastos,
        BigDecimal efectivoEsperadoParcial
) {
}
