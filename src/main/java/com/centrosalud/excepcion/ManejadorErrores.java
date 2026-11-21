package com.centrosalud.excepcion;

import com.centrosalud.util.Tiempo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Captura TODAS las excepciones y las convierte en respuestas JSON ordenadas (@RestControllerAdvice). */
@RestControllerAdvice
public class ManejadorErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    private ResponseEntity<ErrorRes> responder(HttpStatusCode status, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(status).body(new ErrorRes(Tiempo.ahora(), status.value(), mensaje, errores));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorRes> api(ApiException e) {
        return responder(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRes> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errores.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Hay datos invalidos en la solicitud", errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRes> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON valido", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRes> tipoParametro(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "Valor invalido para el parametro '" + e.getName() + "'", null);
    }

    /** Violaciones de FK/UNIQUE de la BD (ej. borrar un paciente que tiene citas). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRes> integridad(DataIntegrityViolationException e) {
        log.warn("Violacion de integridad: {}", e.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "La operacion no se puede completar: hay datos duplicados o registros relacionados", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> general(Exception e) {
        // Muchas excepciones de Spring MVC (404, 405, 415...) ya traen su codigo HTTP: se respeta.
        if (e instanceof ErrorResponse er) {
            String detalle = er.getBody().getDetail();
            return responder(er.getStatusCode(), detalle != null ? detalle : "Solicitud no valida", null);
        }
        log.error("Error no controlado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }
}
