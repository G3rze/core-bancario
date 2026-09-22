package com.banco.core.model.entity;

import com.banco.core.util.PasswordHasher;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SINGLE_TABLE (a diferencia de Usuario, que es @MappedSuperclass): Cajero
 * y Gerente si se consultan juntos como "empleados" (login, busqueda por
 * codigo en ventanilla), asi que una sola tabla con discriminador es mas
 * simple que JOINED para el volumen de este avance academico.
 */
@Entity
@Table(name = "empleados")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_empleado")
public abstract class Empleado extends Usuario {

    private static final AtomicLong CONTADOR_EMPLEADOS = new AtomicLong(1);
    private static final int LARGO_MINIMO_PASSWORD = 6;

    @Column(name = "codigo_empleado", nullable = false, unique = true, updatable = false)
    private final String codigoEmpleado;

    @Column(nullable = false)
    private String sucursal;

    @Column(name = "fecha_contratacion", nullable = false, updatable = false)
    private final LocalDate fechaContratacion;

    @Column(name = "password_hash", nullable = false)
    private final String passwordHash;

    protected Empleado(String dui, String nombre, String direccion, String telefono, String sucursal,
                        String password) {
        super(dui, nombre, direccion, telefono);
        setSucursal(sucursal);
        if (password == null || password.length() < LARGO_MINIMO_PASSWORD) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres");
        }
        this.passwordHash = PasswordHasher.hash(password);
        this.codigoEmpleado = "EMP-%06d".formatted(CONTADOR_EMPLEADOS.getAndIncrement());
        this.fechaContratacion = LocalDate.now();
    }

    protected Empleado() {
        this.codigoEmpleado = null;
        this.fechaContratacion = null;
        this.passwordHash = null;
    }

    public abstract String getNivelAutorizacion();

    public boolean validarPassword(String intento) {
        return intento != null && PasswordHasher.verificar(intento, passwordHash);
    }

    /**
     * Ver Cliente.avanzarContador(): mismo problema (el contador de
     * codigoEmpleado reinicia en cada arranque de la JVM aunque el id ya
     * lo genere Postgres), mismo arreglo.
     */
    public static void avanzarContador(String codigoEmpleado) {
        long valor = Long.parseLong(codigoEmpleado.substring(4));
        CONTADOR_EMPLEADOS.updateAndGet(actual -> Math.max(actual, valor + 1));
    }

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
