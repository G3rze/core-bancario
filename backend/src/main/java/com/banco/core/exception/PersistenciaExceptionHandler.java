package com.banco.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Fallos de la capa de persistencia (base caida, pool agotado, conflictos de
 * concurrencia). La causa completa va al log del servidor; al cliente solo
 * llega un mensaje generico, sin SQL, host ni traza.
 */
@RestControllerAdvice
public class PersistenciaExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PersistenciaExceptionHandler.class);

    /** Dos operaciones chocaron sobre la misma fila (bloqueo optimista o deadlock): reintentar es seguro. */
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, String>> manejarConflicto(ConcurrencyFailureException ex) {
        log.warn("Conflicto de concurrencia en la base de datos", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "La operacion choco con otra en curso. Intente de nuevo."));
    }

    /**
     * Base no disponible o error de acceso a datos. CannotCreateTransactionException
     * (no hay conexion para abrir la transaccion) es un TransactionException, no un
     * DataAccessException, por eso se manejan ambos.
     */
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<Map<String, String>> manejarBaseNoDisponible(Exception ex) {
        log.error("Fallo de acceso a datos", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "El servicio no esta disponible temporalmente. Intente de nuevo mas tarde."));
    }
}
