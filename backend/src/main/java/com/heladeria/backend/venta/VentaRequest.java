package com.heladeria.backend.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record VentaRequest(
        String metodoPago,
        @NotEmpty List<@Valid ItemRequest> items
) {
    public record ItemRequest(
            @NotNull Long productoId,
            Long varianteId,
            List<Long> adicionalIds,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad
    ) {
    }
}
