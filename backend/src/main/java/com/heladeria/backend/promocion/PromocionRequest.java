package com.heladeria.backend.promocion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocionRequest(
        @NotBlank String nombre,
        @NotNull TipoPromocion tipo,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal valor,
        Long productoId,
        String categoria,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        @DecimalMin(value = "0", inclusive = false) BigDecimal cantidadMinima,
        boolean activo
) {
}
