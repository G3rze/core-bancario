package com.banco.core.model.entity;

import java.util.Locale;

/**
 * Tipos de transaccion. {@code etiqueta} es el texto que ya se guarda en los
 * .dat y se muestra en la API ("Deposito", ...): cambiarla invalidaria el
 * historial existente, por eso no se renombra.
 */
public enum TipoTransaccion {
    DEPOSITO("Deposito", true),
    RETIRO("Retiro", false),
    TRANSFERENCIA("Transferencia", false);

    private final String etiqueta;
    private final boolean ingreso;

    TipoTransaccion(String etiqueta, boolean ingreso) {
        this.etiqueta = etiqueta;
        this.ingreso = ingreso;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** true si suma al saldo de la cuenta (ingreso); false si lo resta (egreso). */
    public boolean esIngreso() {
        return ingreso;
    }

    /** Acepta el nombre ("RETIRO") o la etiqueta ("Retiro"), sin distinguir mayusculas. */
    public static TipoTransaccion desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El tipo de transaccion es obligatorio");
        }
        String buscado = texto.trim().toUpperCase(Locale.ROOT);
        for (TipoTransaccion tipo : values()) {
            if (tipo.name().equals(buscado) || tipo.etiqueta.toUpperCase(Locale.ROOT).equals(buscado)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de transaccion desconocido: " + texto);
    }
}
