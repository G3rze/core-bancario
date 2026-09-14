package com.banco.core.model.entity;

public class Cajero extends Empleado {

    private static final long serialVersionUID = 1L;

    private String cajaAsignada;

    public Cajero(String dui, String nombre, String direccion, String telefono, String sucursal, String cajaAsignada) {
        super(dui, nombre, direccion, telefono, sucursal);
        setCajaAsignada(cajaAsignada);
    }

    public void setCajaAsignada(String cajaAsignada) {
        if (cajaAsignada == null || cajaAsignada.isBlank()) {
            throw new IllegalArgumentException("La caja asignada no puede estar vacia");
        }
        this.cajaAsignada = cajaAsignada;
    }

    public String getCajaAsignada() {
        return cajaAsignada;
    }

    @Override
    public String getRol() {
        return "CAJERO";
    }

    @Override
    public String getNivelAutorizacion() {
        return "BASICO";
    }
}
