package com.banco.core.model.entity;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Empleado extends Usuario {

    private static final long serialVersionUID = 1L;

    private static final AtomicLong CONTADOR_EMPLEADOS = new AtomicLong(1);

    private final String codigoEmpleado;
    private String sucursal;
    private final LocalDate fechaContratacion;

    protected Empleado(String dui, String nombre, String direccion, String telefono, String sucursal) {
        super(dui, nombre, direccion, telefono);
        setSucursal(sucursal);
        this.codigoEmpleado = "EMP-%06d".formatted(CONTADOR_EMPLEADOS.getAndIncrement());
        this.fechaContratacion = LocalDate.now();
    }

    public abstract String getNivelAutorizacion();

    public void setSucursal(String sucursal) {
        if (sucursal == null || sucursal.isBlank()) {
            throw new IllegalArgumentException("La sucursal no puede estar vacia");
        }
        this.sucursal = sucursal;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public String getSucursal() {
        return sucursal;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }
}
