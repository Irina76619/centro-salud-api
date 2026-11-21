package com.centrosalud.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Hora oficial del centro (Lima). Los servidores en la nube corren en UTC, por eso no usamos now() a secas. */
public final class Tiempo {
    public static final ZoneId ZONA = ZoneId.of("America/Lima");

    private Tiempo() { }

    public static LocalDateTime ahora() { return LocalDateTime.now(ZONA); }
    public static LocalDate hoy() { return LocalDate.now(ZONA); }
}
