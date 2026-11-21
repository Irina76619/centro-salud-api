package com.centrosalud.excepcion;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato unico de error que recibe el frontend. "errores" trae el detalle por campo en validaciones. */
public record ErrorRes(LocalDateTime timestamp, int status, String mensaje, Map<String, String> errores) {
}
