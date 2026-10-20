package com.heladeria.backend.caja;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MovimientoCajaResponse(
        Long id,
        TipoMovimientoCaja tipo,
        BigDecimal monto,
        String motivo,
        String usuario,
        OffsetDateTime fecha
) {
    static MovimientoCajaResponse desde(MovimientoCaja movimiento) {
        return new MovimientoCajaResponse(
                movimiento.getId(),
                movimiento.getTipo(),
                movimiento.getMonto(),
                movimiento.getMotivo(),
                movimiento.getUsuarioNombreMomento(),
                movimiento.getFecha()
        );
    }
}
