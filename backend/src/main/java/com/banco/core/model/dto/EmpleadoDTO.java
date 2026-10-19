package com.banco.core.model.dto;

import com.banco.core.model.entity.Empleado;

public record EmpleadoDTO(
        String codigoEmpleado,
        String dui,
        String nombre,
        String rol,
        String sucursal,
        String nivelAutorizacion) {

    public static EmpleadoDTO desde(Empleado empleado) {
        return new EmpleadoDTO(
                empleado.getCodigoEmpleado(),
                empleado.getDui(),
                empleado.getNombre(),
                empleado.getRol().name(),
                empleado.getSucursal(),
                empleado.getNivelAutorizacion());
    }
}
