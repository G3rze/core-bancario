package com.banco.core.model.entity;

import java.math.BigDecimal;

public class Transferencia extends Transaccion {

    private final Cuenta origen;
    private final Cuenta destino;

    public Transferencia(Cuenta origen, Cuenta destino, BigDecimal monto) {
        super(monto);
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("La transferencia debe tener cuenta origen y destino");
        }
        this.origen = origen;
        this.destino = destino;
    }

    @Override
    public boolean validarTransaccion() {
        return origen.getEstado() == EstadoCuenta.ACTIVA
                && destino.getEstado() == EstadoCuenta.ACTIVA
                && !origen.equals(destino);
    }

    @Override
    protected void aplicar() {
        origen.retirar(getMonto());
        destino.depositar(getMonto());
    }

    @Override
    public String generarComprobante() {
        return "Transferencia %s | De %s a %s | Monto %s | Estado %s".formatted(
                getNumeroTransaccion(), origen.getNumeroCuenta(), destino.getNumeroCuenta(),
                getMonto(), getEstado());
    }

    public Cuenta getOrigen() {
        return origen;
    }

    public Cuenta getDestino() {
        return destino;
    }
}
