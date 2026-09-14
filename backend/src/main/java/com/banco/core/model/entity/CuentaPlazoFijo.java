package com.banco.core.model.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class CuentaPlazoFijo extends Cuenta {

    private static final long serialVersionUID = 1L;

    private static final AtomicLong CONTADOR_PLAZO_FIJO = new AtomicLong(1);

    private static final BigDecimal MONTO_MINIMO_APERTURA = new BigDecimal("500.00");
    // El concepto de "limite diario" no aplica a un plazo fijo (no hay
    // retiros parciales, se retira el saldo completo al vencer), por lo que
    // aqui es un valor formal sin efecto: retirar() esta sobrescrito por
    // completo y nunca consulta este campo.
    private static final BigDecimal LIMITE_RETIRO_DIARIO = BigDecimal.ZERO;
    private static final BigDecimal TASA_INTERES_ANUAL = new BigDecimal("0.06");
    private static final Set<Integer> PLAZOS_VALIDOS = Set.of(3, 6, 12, 24);

    private final int plazoMeses;
    private final LocalDateTime fechaVencimiento;

    public CuentaPlazoFijo(Cliente titular, BigDecimal saldoInicial, int plazoMeses) {
        super("PLZ-%06d".formatted(CONTADOR_PLAZO_FIJO.getAndIncrement()), titular, saldoInicial,
                MONTO_MINIMO_APERTURA, LIMITE_RETIRO_DIARIO);
        if (!PLAZOS_VALIDOS.contains(plazoMeses)) {
            throw new IllegalArgumentException("El plazo debe ser uno de " + PLAZOS_VALIDOS + " meses");
        }
        this.plazoMeses = plazoMeses;
        this.fechaVencimiento = getFechaApertura().plusMonths(plazoMeses);
    }

    @Override
    public void depositar(BigDecimal monto) {
        // Fondos bloqueados durante todo el plazo: el saldo inicial se fija
        // en el constructor via super(...), no hay depositos adicionales.
        throw new UnsupportedOperationException(
                "No se permiten depositos adicionales en una cuenta a plazo fijo");
    }

    @Override
    public void retirar(BigDecimal monto) {
        // Override completo (no delegado a Cuenta.retirar): el limite de
        // retiro diario del metodo base no tiene sentido aqui, ya que un
        // plazo fijo no admite retiros parciales, solo el saldo completo
        // una vez vencido el plazo.
        if (LocalDateTime.now().isBefore(fechaVencimiento)) {
            throw new UnsupportedOperationException(
                    "Los fondos estan bloqueados hasta " + fechaVencimiento);
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser positivo");
        }
        if (monto.compareTo(getSaldo()) > 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        ajustarSaldo(monto.negate());
    }

    @Override
    public BigDecimal calcularInteres() {
        // Interes total del plazo completo (tasa anual prorrateada a los
        // meses contratados), valor placeholder para este avance academico.
        return getSaldo().multiply(TASA_INTERES_ANUAL).multiply(BigDecimal.valueOf(plazoMeses))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getTipoCuenta() {
        return "Cuenta a Plazo Fijo";
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }
}
