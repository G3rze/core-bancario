package com.banco.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("GERENTE")
public class Gerente extends Empleado {

    @Column(name = "monto_maximo_aprobacion")
    private BigDecimal montoMaximoAprobacion;

    public Gerente(String dui, String nombre, String direccion, String telefono, String sucursal,
                    BigDecimal montoMaximoAprobacion, String password) {
        super(dui, nombre, direccion, telefono, sucursal, password);
        setMontoMaximoAprobacion(montoMaximoAprobacion);
    }

    protected Gerente() {
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
