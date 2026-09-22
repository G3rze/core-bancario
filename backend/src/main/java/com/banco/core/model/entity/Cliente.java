package com.banco.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    private static final AtomicLong CONTADOR_CLIENTES = new AtomicLong(1);

    @Column(name = "numero_cliente", nullable = false, unique = true, updatable = false)
    private final String numeroCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCliente tipo;

    // Transitorio a proposito (no @OneToMany): CuentaServiceImpl ya no
    // confia en este campo para consultas (indexa Cuenta por numero via
    // CuentaRepository), asi que mapearlo como asociacion JPA solo sumaria
    // el riesgo de una coleccion lazy de Hibernate sin necesidad real. Se
    // sigue poblando a mano via agregarCuenta(), igual que antes de la
    // migracion a Postgres.
    @Transient
    private final List<Cuenta> cuentas = new ArrayList<>();

    public Cliente(String dui, String nombre, String direccion, String telefono, TipoCliente tipo) {
        super(dui, nombre, direccion, telefono);
        setTipo(tipo);
        this.numeroCliente = "CLI-%06d".formatted(CONTADOR_CLIENTES.getAndIncrement());
    }

    protected Cliente() {
        this.numeroCliente = null;
    }

    /**
     * El id lo genera Postgres (IDENTITY), pero numeroCliente sigue siendo
     * un AtomicLong en memoria que reinicia en cada arranque de la JVM; sin
     * esto, un cliente nuevo tras un reinicio podria repetir el numero de
     * uno ya persistido. ClienteServiceImpl la llama por cada cliente
     * cargado del repository al arrancar (mismo patron que antes de migrar
     * a Postgres, solo que la fuente ahora es la base en vez del .dat).
     */
    public static void avanzarContador(String numeroCliente) {
        long valor = Long.parseLong(numeroCliente.substring(4));
        CONTADOR_CLIENTES.updateAndGet(actual -> Math.max(actual, valor + 1));
    }

    public void setTipo(TipoCliente tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        this.tipo = tipo;
    }

    public void agregarCuenta(Cuenta cuenta) {
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta no puede ser nula");
        }
        if (cuentas.contains(cuenta)) {
            throw new IllegalArgumentException("La cuenta ya esta registrada para este cliente");
        }
        cuentas.add(cuenta);
    }

    public void eliminarCuenta(String numeroCuenta) {
        boolean eliminada = cuentas.removeIf(c -> c.getNumeroCuenta().equals(numeroCuenta));
        if (!eliminada) {
            throw new IllegalArgumentException("El cliente no tiene una cuenta con ese numero");
        }
    }

    public List<Cuenta> getCuentas() {
        return Collections.unmodifiableList(cuentas);
    }

    public String getNumeroCliente() {
        return numeroCliente;
    }

    public TipoCliente getTipo() {
        return tipo;
    }

    @Override
    public String getRol() {
        return "CLIENTE";
    }
}
