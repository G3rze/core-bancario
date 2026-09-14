package com.banco.core.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Cuenta implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private final String numeroCuenta;
    private final Cliente titular;

    // No existe setSaldo(): el saldo solo cambia a traves de depositar()/
    // retirar() (o de las restricciones que cada subtipo les imponga), nunca
    // de forma directa desde fuera de la clase.
    private BigDecimal saldo;

    private final BigDecimal montoMinimoApertura;
    private final BigDecimal limiteRetiroDiario;
    private final LocalDateTime fechaApertura;
    private EstadoCuenta estado;

    protected Cuenta(String numeroCuenta, Cliente titular, BigDecimal saldoInicial,
                      BigDecimal montoMinimoApertura, BigDecimal limiteRetiroDiario) {
        if (titular == null) {
            throw new IllegalArgumentException("La cuenta debe tener un titular");
        }
        if (saldoInicial == null || montoMinimoApertura == null
                || saldoInicial.compareTo(montoMinimoApertura) < 0) {
            throw new IllegalArgumentException(
                    "El deposito inicial debe ser al menos " + montoMinimoApertura);
        }
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.saldo = saldoInicial;
        this.montoMinimoApertura = montoMinimoApertura;
        this.limiteRetiroDiario = limiteRetiroDiario;
        this.fechaApertura = LocalDateTime.now();
        this.estado = EstadoCuenta.ACTIVA;
    }

    public abstract BigDecimal calcularInteres();

    public abstract String getTipoCuenta();

    public void depositar(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser positivo");
        }
        ajustarSaldo(monto);
    }

    public void retirar(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser positivo");
        }
        if (monto.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        if (monto.compareTo(limiteRetiroDiario) > 0) {
            throw new IllegalArgumentException("El monto excede el limite de retiro diario");
        }
        ajustarSaldo(monto.negate());
    }

    /**
     * Unico punto por el que el saldo cambia de valor. Es {@code protected}
     * para que subtipos con reglas de saldo distintas (p. ej. sobregiro en
     * CuentaCorriente) puedan aplicarlas tras su propia validacion, sin
     * exponer jamas una mutacion directa del saldo fuera de la jerarquia.
     */
    protected void ajustarSaldo(BigDecimal delta) {
        this.saldo = this.saldo.add(delta);
    }

    public void activar() {
        this.estado = EstadoCuenta.ACTIVA;
    }

    public void bloquear() {
        this.estado = EstadoCuenta.BLOQUEADA;
    }

    public void cerrar() {
        this.estado = EstadoCuenta.CERRADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public Cliente getTitular() {
        return titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public BigDecimal getMontoMinimoApertura() {
        return montoMinimoApertura;
    }

    public BigDecimal getLimiteRetiroDiario() {
        return limiteRetiroDiario;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cuenta otra)) {
            return false;
        }
        return Objects.equals(numeroCuenta, otra.numeroCuenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroCuenta);
    }

    @Override
    public String toString() {
        return "%s{numeroCuenta='%s', saldo=%s, estado=%s}".formatted(
                getClass().getSimpleName(), numeroCuenta, saldo, estado);
    }
}
