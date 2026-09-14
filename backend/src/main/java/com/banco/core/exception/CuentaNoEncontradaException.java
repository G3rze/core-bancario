package com.banco.core.exception;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(String message) {
        super(message);
    }

    public static CuentaNoEncontradaException porNumero(String numeroCuenta) {
        return new CuentaNoEncontradaException("No existe una cuenta con numero " + numeroCuenta);
    }
}
