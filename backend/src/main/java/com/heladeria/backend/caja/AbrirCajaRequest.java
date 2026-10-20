package com.heladeria.backend.caja;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AbrirCajaRequest(
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal montoInicial
) {
}
