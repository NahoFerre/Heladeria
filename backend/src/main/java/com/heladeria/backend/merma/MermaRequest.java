package com.heladeria.backend.merma;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MermaRequest(
        Long insumoId,
        Long productoId,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
        @NotNull MotivoMerma motivo,
        @Size(max = 300) String observacion
) {
}
