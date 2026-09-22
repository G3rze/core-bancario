package com.banco.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @MappedSuperclass (no @Entity): Cliente y Empleado nunca se consultan
 * como "todos los Usuario" juntos -son conceptos de negocio distintos con
 * tablas propias (clientes, empleados)-, asi que no amerita una jerarquia
 * polimorfica compartida en la base de datos. Los campos de aqui se
 * heredan como columnas normales en cada tabla concreta.
 */
@MappedSuperclass
public abstract class Usuario {

    private static final Pattern PATRON_DUI = Pattern.compile("^\\d{8}-\\d$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{4}-\\d{4}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true: dui esta @MappedSuperclass, asi que esta constraint se
    // aplica por separado en cada tabla concreta (clientes.dui,
    // empleados.dui) - correcto, cada una necesita su propia unicidad.
    @Column(nullable = false, unique = true)
    private String dui;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false, updatable = false)
    private final LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private boolean activo;

    protected Usuario(String dui, String nombre, String direccion, String telefono) {
        setDui(dui);
        setNombre(nombre);
        setDireccion(direccion);
        setTelefono(telefono);
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }

    /** Constructor sin argumentos exigido por JPA; no usar directamente. */
    protected Usuario() {
        this.fechaRegistro = null;
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
