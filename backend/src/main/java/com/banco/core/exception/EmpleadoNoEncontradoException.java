package com.banco.core.exception;

public class EmpleadoNoEncontradoException extends RuntimeException {

    public EmpleadoNoEncontradoException(String message) {
        super(message);
    }

    public static EmpleadoNoEncontradoException porCodigo(String codigoEmpleado) {
        return new EmpleadoNoEncontradoException("No existe un empleado con codigo " + codigoEmpleado);
    }
}
