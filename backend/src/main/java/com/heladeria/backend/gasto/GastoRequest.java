package com.heladeria.backend.gasto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GastoRequest(
        String categoria,
        @NotBlank String descripcion,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal monto,
        String metodoPago,
        @Size(max = 300) String observacion
) {
}
