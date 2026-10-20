package com.banco.core.security;

import org.springframework.security.core.Authentication;

/**
 * Regla de propiedad de las cuentas, separada de los controllers: estos solo
 * la invocan desde @PreAuthorize y no saben de repositorios ni de entidades.
 * Una sola operacion a proposito (interfaz minima): hoy toda la pregunta es
 * "puede esta persona operar esta cuenta?".
 */
public interface AutorizacionCuentas {

    /**
     * CAJERO y GERENTE operan cualquier cuenta; un CLIENTE solo las que
     * titula. Cualquier otro rol (p. ej. ADMIN, que solo gestiona usuarios)
     * o una peticion sin autenticar: false.
     */
    boolean puedeOperar(String numeroCuenta, Authentication autenticacion);
}
