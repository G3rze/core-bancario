package com.banco.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Errores del dominio bancario en si (Cliente/Cuenta/Transaccion) —
 * ClienteController, CuentaController. Los errores de autenticacion/
 * autorizacion de empleados viven en un @RestControllerAdvice aparte,
 * SeguridadExceptionHandler: en vez de un unico handler global cubriendo
 * todo, cada @RestControllerAdvice cubre un grupo de excepciones de un
 * mismo dominio.
 */
@RestControllerAdvice
public class NegocioExceptionHandler {

    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class})
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({SaldoInsuficienteException.class, TransaccionInvalidaException.class,
            IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
