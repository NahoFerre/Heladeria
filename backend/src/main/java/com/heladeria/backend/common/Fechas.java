package com.heladeria.backend.common;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

// Convierte un filtro de fecha (LocalDate, tal como llega de los selectores de fecha del frontend)
// a los límites de OffsetDateTime del día completo, en la zona horaria del servidor.
public final class Fechas {

    private Fechas() {
    }

    public static OffsetDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    public static OffsetDateTime finDelDia(LocalDate fecha) {
        return fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);
    }
}
