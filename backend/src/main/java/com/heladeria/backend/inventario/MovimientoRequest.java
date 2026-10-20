package com.heladeria.backend.inventario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimientoRequest(
        @NotNull TipoMovimiento tipo,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal cantidad,
        String motivo
) {
}
