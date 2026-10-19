package com.banco.core.model.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Empleado con rol ADMIN: activa/desactiva usuarios. No agrega campos
 * propios; existe como subtipo para que el rol sea polimorfico igual que
 * Cajero y Gerente (SINGLE_TABLE en empleados, discriminador "ADMIN").
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Administrador extends Empleado {

    public Administrador(String dui, String nombre, String direccion, String telefono, String sucursal,
                         String passwordHash) {
        super(dui, nombre, direccion, telefono, sucursal, passwordHash);
    }

    protected Administrador() {
    }

    @Override
    public Rol getRol() {
        return Rol.ADMIN;
    }

    @Override
    public String getNivelAutorizacion() {
        return "TOTAL";
    }
}
