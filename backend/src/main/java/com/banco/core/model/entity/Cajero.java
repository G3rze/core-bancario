package com.banco.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAJERO")
public class Cajero extends Empleado {

    @Column(name = "caja_asignada")
    private String cajaAsignada;

    public Cajero(String dui, String nombre, String direccion, String telefono, String sucursal,
                  String cajaAsignada, String passwordHash) {
        super(dui, nombre, direccion, telefono, sucursal, passwordHash);
        setCajaAsignada(cajaAsignada);
    }

    protected Cajero() {
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
    public Rol getRol() {
        return Rol.CAJERO;
    }

    @Override
    public String getNivelAutorizacion() {
        return "BASICO";
    }
}
