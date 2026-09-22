package com.banco.core.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * TODO: centralizar aqui los errores de autenticacion y
 * autorizacion de empleados (login, alta de empleados, ventanilla) —
 * separado de NegocioExceptionHandler (que cubre Cliente/Cuenta/
 * Transaccion) para no mezclar los dos dominios en un mismo
 * @RestControllerAdvice.
 * <p>
 * Agregar un metodo {@code @ExceptionHandler} por cada una de estas (mismo
 * patron que NegocioExceptionHandler:
 * {@code ResponseEntity.status(...).body(Map.of("error", ex.getMessage()))}):
 * <ul>
 *   <li>{@code CredencialesInvalidasException} -&gt; 401 UNAUTHORIZED
 *       (login con dui/password incorrectos).
 *   <li>{@code AccesoNoAutorizadoException} -&gt; 403 FORBIDDEN (crear
 *       empleado sin ser Gerente).
 *   <li>{@code EmpleadoNoEncontradoException} -&gt; 404 NOT_FOUND (codigo
 *       de cajero que no existe, en ventanilla).
 * </ul>
 * Mientras esto quede vacio, esas tres excepciones caen en el manejo por
 * defecto de Spring (500) en vez del status HTTP correcto — no rompe nada
 * que ya funcione, porque los controllers que las lanzan (AuthController,
 * EmpleadoController, VentanillaController) todavia estan en TODO tambien.
 */
@RestControllerAdvice
public class SeguridadExceptionHandler {
}
