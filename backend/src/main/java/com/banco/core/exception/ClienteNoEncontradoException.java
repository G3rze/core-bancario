package com.banco.core.exception;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(String message) {
        super(message);
    }

    public static ClienteNoEncontradoException porDui(String dui) {
        return new ClienteNoEncontradoException("No existe un cliente con DUI " + dui);
    }
}
