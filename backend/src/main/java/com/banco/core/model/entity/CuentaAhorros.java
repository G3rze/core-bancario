package com.banco.core.model.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

public class CuentaAhorros extends Cuenta {

    private static final long serialVersionUID = 1L;

    private static final AtomicLong CONTADOR_AHORROS = new AtomicLong(1);

    private static final BigDecimal MONTO_MINIMO_APERTURA = new BigDecimal("25.00");
    private static final BigDecimal LIMITE_RETIRO_DIARIO = new BigDecimal("500.00");
    private static final BigDecimal TASA_INTERES_ANUAL = new BigDecimal("0.02");

    public CuentaAhorros(Cliente titular, BigDecimal saldoInicial) {
        super("AHO-%06d".formatted(CONTADOR_AHORROS.getAndIncrement()), titular, saldoInicial,
                MONTO_MINIMO_APERTURA, LIMITE_RETIRO_DIARIO);
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
