package com.banco.core.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String message) {
        super(message);
    }

    public static UsuarioNoEncontradoException porDui(String dui) {
        return new UsuarioNoEncontradoException("No existe un usuario con DUI " + dui);
    }
}
