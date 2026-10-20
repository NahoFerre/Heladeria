package com.heladeria.backend.producto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ConfigurarRecetaRequest(
        List<@Valid ItemRecetaRequest> recetaProducto,
        List<@Valid RecetaVarianteRequest> recetasVariantes
) {
    public record ItemRecetaRequest(
            @NotNull Long insumoId,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad
    ) {
    }

    public record RecetaVarianteRequest(
            @NotNull Long varianteId,
            List<@Valid ItemRecetaRequest> items
    ) {
    }
}
