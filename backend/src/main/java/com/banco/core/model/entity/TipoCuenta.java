package com.banco.core.model.entity;

import java.util.Locale;

/**
 * Tipos de cuenta que se pueden abrir. Los nombres coinciden con el
 * discriminador de la tabla y con el valor que ya manda el frontend
 * ("AHORROS", "CORRIENTE", "PLAZO_FIJO").
 */
public enum TipoCuenta {
    AHORROS,
    CORRIENTE,
    PLAZO_FIJO;

    /** Insensible a mayusculas (como el switch anterior); nulo o desconocido lanza IllegalArgumentException. */
    public static TipoCuenta desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio");
        }
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de cuenta desconocido: " + texto, e);
        }
    }
}
