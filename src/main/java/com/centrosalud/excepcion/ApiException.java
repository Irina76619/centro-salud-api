package com.centrosalud.excepcion;

import org.springframework.http.HttpStatus;

/**
 * Error "esperado" de negocio. Lleva el codigo HTTP que debe recibir el frontend.
 * Reemplaza a los System.out.println("...") de la version consola.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static ApiException noEncontrado(String entidad, Object id) {
        return new ApiException(HttpStatus.NOT_FOUND, entidad + " con id " + id + " no existe");
    }
    public static ApiException conflicto(String mensaje) { return new ApiException(HttpStatus.CONFLICT, mensaje); }
    public static ApiException reglaNegocio(String mensaje) { return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, mensaje); }
    public static ApiException prohibido(String mensaje) { return new ApiException(HttpStatus.FORBIDDEN, mensaje); }
    public static ApiException noAutorizado(String mensaje) { return new ApiException(HttpStatus.UNAUTHORIZED, mensaje); }
    public static ApiException demasiadosIntentos(String mensaje) { return new ApiException(HttpStatus.TOO_MANY_REQUESTS, mensaje); }
}
