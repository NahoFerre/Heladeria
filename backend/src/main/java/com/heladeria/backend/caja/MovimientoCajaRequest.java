package com.heladeria.backend.caja;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MovimientoCajaRequest(
        @NotNull TipoMovimientoCaja tipo,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal monto,
        @NotBlank @Size(max = 300) String motivo
) {
}
