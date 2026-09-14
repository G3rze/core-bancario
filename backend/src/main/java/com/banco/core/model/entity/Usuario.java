package com.banco.core.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern PATRON_DUI = Pattern.compile("^\\d{8}-\\d$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{4}-\\d{4}$");

    private Long id;
    private String dui;
    private String nombre;
    private String direccion;
    private String telefono;
    private final LocalDateTime fechaRegistro;
    private boolean activo;

    protected Usuario(String dui, String nombre, String direccion, String telefono) {
        setDui(dui);
        setNombre(nombre);
        setDireccion(direccion);
        setTelefono(telefono);
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }

    public abstract String getRol();

    public void setDui(String dui) {
        if (dui == null || !PATRON_DUI.matcher(dui).matches()) {
            throw new IllegalArgumentException("El DUI debe tener el formato 00000000-0");
        }
        this.dui = dui;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) {
        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("La direccion no puede estar vacia");
        }
        this.direccion = direccion;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || !PATRON_TELEFONO.matcher(telefono).matches()) {
            throw new IllegalArgumentException("El telefono debe tener el formato 0000-0000");
        }
        this.telefono = telefono;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDui() {
        return dui;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Usuario otro)) {
            return false;
        }
        return Objects.equals(dui, otro.dui);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dui);
    }

    @Override
    public String toString() {
        return "%s{dui='%s', nombre='%s', rol='%s'}".formatted(
                getClass().getSimpleName(), dui, nombre, getRol());
    }
}
