package com.banco.core.model.entity;

import java.math.BigDecimal;

public class Gerente extends Empleado {

    private static final long serialVersionUID = 1L;

    private BigDecimal montoMaximoAprobacion;

    public Gerente(String dui, String nombre, String direccion, String telefono, String sucursal,
                    BigDecimal montoMaximoAprobacion) {
        super(dui, nombre, direccion, telefono, sucursal);
        setMontoMaximoAprobacion(montoMaximoAprobacion);
    }

    public void setMontoMaximoAprobacion(BigDecimal montoMaximoAprobacion) {
        if (montoMaximoAprobacion == null || montoMaximoAprobacion.signum() <= 0) {
            throw new IllegalArgumentException("El monto maximo de aprobacion debe ser positivo");
        }
        this.montoMaximoAprobacion = montoMaximoAprobacion;
    }

    public BigDecimal getMontoMaximoAprobacion() {
        return montoMaximoAprobacion;
    }

    @Override
    public String getRol() {
        return "GERENTE";
    }

    @Override
    public String getNivelAutorizacion() {
        return "SUPERVISOR";
    }
}
