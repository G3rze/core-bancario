package com.banco.core.model.entity;

import java.math.BigDecimal;

public class Retiro extends Transaccion {

    private final Cuenta cuenta;

    public Retiro(Cuenta cuenta, BigDecimal monto) {
        super(monto);
        if (cuenta == null) {
            throw new IllegalArgumentException("El retiro debe tener una cuenta origen");
        }
        this.cuenta = cuenta;
    }

    @Override
    public boolean validarTransaccion() {
        return cuenta.getEstado() == EstadoCuenta.ACTIVA;
    }

    @Override
    protected void aplicar() {
        cuenta.retirar(getMonto());
    }

    @Override
    public String generarComprobante() {
        return "Retiro %s | Cuenta %s | Monto %s | Estado %s".formatted(
                getNumeroTransaccion(), cuenta.getNumeroCuenta(), getMonto(), getEstado());
    }

    public Cuenta getCuenta() {
        return cuenta;
    }
}
