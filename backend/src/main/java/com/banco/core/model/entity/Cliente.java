package com.banco.core.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Cliente extends Usuario {

    private static final long serialVersionUID = 1L;

    private static final AtomicLong CONTADOR_CLIENTES = new AtomicLong(1);

    private final String numeroCliente;
    private TipoCliente tipo;

    // Lista de cuentas del cliente: acceso mayormente iterativo (mostrar
    // cuentas del cliente) con appends ocasionales al abrir una cuenta nueva;
    // un cliente realista maneja pocas cuentas, por lo que ArrayList es
    // suficiente y mas simple que LinkedList para este patron de uso.
    private final List<Cuenta> cuentas = new ArrayList<>();

    public Cliente(String dui, String nombre, String direccion, String telefono, TipoCliente tipo) {
        super(dui, nombre, direccion, telefono);
        setTipo(tipo);
        this.numeroCliente = "CLI-%06d".formatted(CONTADOR_CLIENTES.getAndIncrement());
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
