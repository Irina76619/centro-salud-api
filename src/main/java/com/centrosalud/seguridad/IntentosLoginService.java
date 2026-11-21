package com.centrosalud.seguridad;

import com.centrosalud.excepcion.ApiException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Version web de la regla "maximo 3 intentos": en vez de cerrar el programa, se BLOQUEA el acceso
 * a ese usuario durante 5 minutos tras 3 fallos seguidos. Se guarda en memoria (se reinicia con el servidor).
 */
@Service
public class IntentosLoginService {

    public static final int MAX_INTENTOS = 3;
    private static final Duration BLOQUEO = Duration.ofMinutes(5);

    private record Estado(int fallos, Instant bloqueadoHasta) { }

    private final Map<String, Estado> estados = new ConcurrentHashMap<>();

    public void verificarNoBloqueado(String clave) {
        Estado e = estados.get(clave);
        if (e != null && e.bloqueadoHasta() != null && Instant.now().isBefore(e.bloqueadoHasta())) {
            throw ApiException.demasiadosIntentos("Demasiados intentos fallidos. Espera 5 minutos e intenta de nuevo.");
        }
    }

    /** Registra un fallo y devuelve cuantos intentos quedan. */
    public int registrarFallo(String clave) {
        Estado nuevo = estados.compute(clave, (k, e) -> {
            boolean bloqueoVencido = e != null && e.bloqueadoHasta() != null && !Instant.now().isBefore(e.bloqueadoHasta());
            int fallos = (e == null || bloqueoVencido ? 0 : e.fallos()) + 1;
            return fallos >= MAX_INTENTOS ? new Estado(fallos, Instant.now().plus(BLOQUEO)) : new Estado(fallos, null);
        });
        return MAX_INTENTOS - nuevo.fallos();
    }

    public void limpiar(String clave) {
        estados.remove(clave);
    }
}
