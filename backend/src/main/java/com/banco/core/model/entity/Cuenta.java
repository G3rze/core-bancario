package com.banco.core.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * SINGLE_TABLE con discriminador `tipo_cuenta_clase` (nombre distinto de
 * `tipoCuenta`, que ya es el getter del nombre para mostrar, p. ej.
 * "Cuenta de Ahorros"): una sola tabla `cuentas` es mas simple que JOINED
 * para el volumen de este avance academico, con columnas nullable para los
 * campos que solo aplican a un subtipo (p. ej. plazoMeses en CuentaPlazoFijo).
 */
@Entity
@Table(name = "cuentas")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cuenta_clase")
public abstract class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, updatable = false)
    private final String numeroCuenta;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    private final Cliente titular;

    // No existe setSaldo(): el saldo solo cambia a traves de depositar()/
    // retirar() (o de las restricciones que cada subtipo les imponga), nunca
    // de forma directa desde fuera de la clase.
    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(name = "monto_minimo_apertura", nullable = false, updatable = false)
    private final BigDecimal montoMinimoApertura;

    @Column(name = "limite_retiro_diario", nullable = false, updatable = false)
    private final BigDecimal limiteRetiroDiario;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private final LocalDateTime fechaApertura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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

    /** Constructor sin argumentos exigido por JPA; no usar directamente. */
    protected Cuenta() {
        this.numeroCuenta = null;
        this.titular = null;
        this.montoMinimoApertura = null;
        this.limiteRetiroDiario = null;
        this.fechaApertura = null;
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
