package com.banco.core.model.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@DiscriminatorValue("CORRIENTE")
public class CuentaCorriente extends Cuenta {

    private static final AtomicLong CONTADOR_CORRIENTE = new AtomicLong(1);

    private static final BigDecimal MONTO_MINIMO_APERTURA = new BigDecimal("100.00");
    private static final BigDecimal LIMITE_RETIRO_DIARIO = new BigDecimal("2000.00");
    private static final BigDecimal LIMITE_SOBREGIRO = new BigDecimal("200.00");

    public CuentaCorriente(Cliente titular, BigDecimal saldoInicial) {
        super("COR-%06d".formatted(CONTADOR_CORRIENTE.getAndIncrement()), titular, saldoInicial,
                MONTO_MINIMO_APERTURA, LIMITE_RETIRO_DIARIO);
    }

    protected CuentaCorriente() {
    }

    /**
     * Ver Cliente.avanzarContador(): mismo problema (el contador de
     * numeroCuenta reinicia en cada arranque de la JVM aunque el id ya lo
     * genere Postgres), mismo arreglo.
     */
    public static void avanzarContador(String numeroCuenta) {
        long valor = Long.parseLong(numeroCuenta.substring(4));
        CONTADOR_CORRIENTE.updateAndGet(actual -> Math.max(actual, valor + 1));
    }

    @Override
    public void retirar(BigDecimal monto) {
        // Override completo (no delegado a Cuenta.retirar): a diferencia de
        // las demas cuentas, CuentaCorriente permite saldo negativo hasta
        // LIMITE_SOBREGIRO, por lo que la validacion "saldo >= monto" del
        // metodo base no aplica aqui.
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser positivo");
        }
        if (monto.compareTo(getLimiteRetiroDiario()) > 0) {
            throw new IllegalArgumentException("El monto excede el limite de retiro diario");
        }
        BigDecimal saldoResultante = getSaldo().subtract(monto);
        if (saldoResultante.compareTo(LIMITE_SOBREGIRO.negate()) < 0) {
            throw new IllegalArgumentException("El retiro excede el limite de sobregiro permitido");
        }
        ajustarSaldo(monto.negate());
    }

    @Override
    public BigDecimal calcularInteres() {
        // Regla de negocio: las cuentas corrientes no generan intereses.
        // Se sobrescribe explicitamente (en vez de heredar un comportamiento
        // por defecto) para que quede claro que es una decision deliberada.
        return BigDecimal.ZERO;
    }

    @Override
    public String getTipoCuenta() {
        return "Cuenta Corriente";
    }
}
