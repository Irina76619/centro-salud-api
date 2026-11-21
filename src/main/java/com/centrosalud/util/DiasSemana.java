package com.centrosalud.util;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

/** Utilidades para los dias de la semana en espanol (Lunes ... Domingo), tolerante a tildes y mayusculas. */
public final class DiasSemana {
    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    private DiasSemana() { }

    /** Nombre del dia para una fecha concreta. */
    public static String deFecha(LocalDate fecha) {
        return DIAS[fecha.getDayOfWeek().getValue() - 1];
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Devuelve el nombre canonico ("miercoles" -> "Miércoles") o null si no es un dia valido. */
    public static String canonico(String entrada) {
        if (entrada == null) return null;
        for (String d : DIAS) {
            if (normalizar(d).equals(normalizar(entrada))) return d;
        }
        return null;
    }

    public static boolean mismoDia(String a, String b) {
        return a != null && b != null && normalizar(a).equals(normalizar(b));
    }

    /** Posicion en la semana (0 = Lunes) para ordenar horarios. */
    public static int indice(String dia) {
        for (int i = 0; i < DIAS.length; i++) {
            if (mismoDia(DIAS[i], dia)) return i;
        }
        return 99;
    }
}
