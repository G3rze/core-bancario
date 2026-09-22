package com.banco.core.exception;

public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException(String message) {
        super(message);
    }
}
