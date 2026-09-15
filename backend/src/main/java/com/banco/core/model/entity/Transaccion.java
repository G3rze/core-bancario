package com.banco.core.model.entity;

import com.banco.core.exception.SaldoInsuficienteException;
import com.banco.core.exception.TransaccionInvalidaException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Transaccion implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final AtomicLong CONTADOR_TRANSACCIONES = new AtomicLong(1);

    private Long id;
    private final String numeroTransaccion;
    private final BigDecimal monto;
    private final LocalDateTime fecha;
    private EstadoTransaccion estado;

    protected Transaccion(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto de la transaccion debe ser positivo");
        }
        this.numeroTransaccion = "TRX-%06d".formatted(CONTADOR_TRANSACCIONES.getAndIncrement());
        this.monto = monto;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoTransaccion.PENDIENTE;
    }

    public abstract boolean validarTransaccion();

    public abstract String generarComprobante();

    protected abstract void aplicar();

    /**
     * Punto unico de ejecucion: valida, delega la mutacion real en las
     * reglas ya existentes de Cuenta (aplicar()) y deja el estado en
     * COMPLETADA o RECHAZADA segun el resultado, sin duplicar aqui las
     * reglas de saldo/limite que cada subtipo de Cuenta ya impone.
     */
    public final void ejecutar() {
        if (!validarTransaccion()) {
            this.estado = EstadoTransaccion.RECHAZADA;
            throw new TransaccionInvalidaException(
                    "La transaccion " + numeroTransaccion + " no paso las validaciones");
        }
        try {
            aplicar();
            this.estado = EstadoTransaccion.COMPLETADA;
        } catch (UnsupportedOperationException e) {
            this.estado = EstadoTransaccion.RECHAZADA;
            throw new TransaccionInvalidaException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            this.estado = EstadoTransaccion.RECHAZADA;
            throw new SaldoInsuficienteException(e.getMessage(), e);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroTransaccion() {
        return numeroTransaccion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Transaccion otra)) {
            return false;
        }
        return Objects.equals(numeroTransaccion, otra.numeroTransaccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroTransaccion);
    }

    @Override
    public String toString() {
        return "%s{numeroTransaccion='%s', monto=%s, estado=%s}".formatted(
                getClass().getSimpleName(), numeroTransaccion, monto, estado);
    }
}
