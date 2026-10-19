package com.banco.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciales del primer gerente y del primer administrador, leidas de
 * .env (prefijo banco.bootstrap). Un record inmutable: solo transporta datos.
 */
@ConfigurationProperties(prefix = "banco.bootstrap")
public record BootstrapProperties(Credencial gerente, Credencial admin) {

    public record Credencial(String dui, String nombre, String password) {

        public boolean estaCompleta() {
            return dui != null && !dui.isBlank() && password != null && !password.isBlank();
        }
    }
}
