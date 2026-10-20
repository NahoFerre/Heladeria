package com.heladeria.backend.producto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ConfigurarOpcionesRequest(
        List<@Valid VarianteRequest> variantes,
        List<@Valid GrupoRequest> gruposAdicionales
) {
    public record VarianteRequest(
            Long id,
            @NotBlank String nombre,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal precio,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal costo,
            String sku,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal stockActual,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal stockMinimo,
            boolean activo
    ) {
    }

    public record GrupoRequest(
            Long id,
            @NotBlank String nombre,
            @Min(0) int minSeleccion,
            @Min(1) int maxSeleccion,
            @NotEmpty List<@Valid AdicionalRequest> opciones
    ) {
        public record AdicionalRequest(
                Long id,
                @NotBlank String nombre,
                @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal precioAdicional,
                BigDecimal costoAdicional,
                boolean controlaStock,
                BigDecimal stockActual,
                BigDecimal stockMinimo,
                boolean activo
        ) {
        }
    }
}
