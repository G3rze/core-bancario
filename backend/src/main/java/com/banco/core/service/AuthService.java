package com.banco.core.service;

import com.banco.core.model.dto.LoginResponse;

/**
 * Inicio de sesion unificado para clientes y empleados. El controller solo
 * conoce este contrato; como se verifican las credenciales y se emite el
 * token es un detalle de la implementacion.
 */
public interface AuthService {

    /** @throws com.banco.core.exception.CredencialesInvalidasException si las credenciales no son validas (401) */
    LoginResponse login(String dui, String password);
}
