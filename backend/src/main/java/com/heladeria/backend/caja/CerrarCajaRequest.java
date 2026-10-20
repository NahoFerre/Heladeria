package com.heladeria.backend.caja;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CerrarCajaRequest(
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal montoContado,
        @Size(max = 300) String observacion
) {
}
