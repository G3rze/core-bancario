package com.banco.core.model.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@DiscriminatorValue("AHORROS")
public class CuentaAhorros extends Cuenta {

    private static final AtomicLong CONTADOR_AHORROS = new AtomicLong(1);

    private static final BigDecimal MONTO_MINIMO_APERTURA = new BigDecimal("25.00");
    private static final BigDecimal LIMITE_RETIRO_DIARIO = new BigDecimal("500.00");
    private static final BigDecimal TASA_INTERES_ANUAL = new BigDecimal("0.02");

    public CuentaAhorros(Cliente titular, BigDecimal saldoInicial) {
        super("AHO-%06d".formatted(CONTADOR_AHORROS.getAndIncrement()), titular, saldoInicial,
                MONTO_MINIMO_APERTURA, LIMITE_RETIRO_DIARIO);
    }

    protected CuentaAhorros() {
    }

    /**
     * Ver Cliente.avanzarContador(): mismo problema (el contador de
     * numeroCuenta reinicia en cada arranque de la JVM aunque el id ya lo
     * genere Postgres), mismo arreglo.
     */
    public static void avanzarContador(String numeroCuenta) {
        long valor = Long.parseLong(numeroCuenta.substring(4));
        CONTADOR_AHORROS.updateAndGet(actual -> Math.max(actual, valor + 1));
    }

    @Override
    public BigDecimal calcularInteres() {
        // Interes mensual simple sobre el saldo actual (valor placeholder
        // para este avance academico).
        return getSaldo().multiply(TASA_INTERES_ANUAL)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getTipoCuenta() {
        return "Cuenta de Ahorros";
    }
}
