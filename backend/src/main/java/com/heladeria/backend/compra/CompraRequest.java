package com.heladeria.backend.compra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CompraRequest(
        String proveedor,
        @Size(max = 50) String numeroFactura,
        String metodoPago,
        @Size(max = 300) String observaciones,
        @NotEmpty List<@Valid ItemRequest> items
) {
    public record ItemRequest(
            Long insumoId,
            Long productoId,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
            @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal costoUnitario
    ) {
    }
}
