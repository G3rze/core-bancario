package com.banco.core.model.entity;

import java.math.BigDecimal;

public class Deposito extends Transaccion {

    private static final long serialVersionUID = 1L;

    private final Cuenta cuenta;

    public Deposito(Cuenta cuenta, BigDecimal monto) {
        super(monto);
        if (cuenta == null) {
            throw new IllegalArgumentException("El deposito debe tener una cuenta destino");
        }
        this.cuenta = cuenta;
    }

    @Override
    public boolean validarTransaccion() {
        return cuenta.getEstado() == EstadoCuenta.ACTIVA;
    }

    @Override
    protected void aplicar() {
        cuenta.depositar(getMonto());
    }

    @Override
    public String generarComprobante() {
        return "Deposito %s | Cuenta %s | Monto %s | Estado %s".formatted(
                getNumeroTransaccion(), cuenta.getNumeroCuenta(), getMonto(), getEstado());
    }

    public Cuenta getCuenta() {
        return cuenta;
    }
}
