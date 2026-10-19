package com.banco.core.util;

/**
 * Regla de contrasenas en un solo lugar. Las entidades solo reciben el hash
 * (ya no ven la contrasena en claro), asi que la validacion de largo ocurre
 * en los servicios antes de codificar: empleados hoy, registro de clientes
 * en P3.
 */
public final class PoliticaPassword {

    public static final int LARGO_MINIMO = 6;

    private PoliticaPassword() {
    }

    public static void validar(String password) {
        if (password == null || password.length() < LARGO_MINIMO) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos " + LARGO_MINIMO + " caracteres");
        }
    }
}
