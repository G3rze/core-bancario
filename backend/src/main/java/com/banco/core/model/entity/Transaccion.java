package com.banco.core.model.entity;

import com.banco.core.exception.SaldoInsuficienteException;
import com.banco.core.exception.TransaccionInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Objeto de negocio vivo, con referencias directas a Cuenta/Cajero: solo
 * existe en memoria durante ejecutar() (dentro de CuentaServiceImpl). Nunca
 * se persiste tal cual -eso arriesgaria serializar entidades JPA fuera de
 * sesion-; justo antes de guardarse se convierte a RegistroTransaccion
 * (log plano, ver esa clase) via RegistroTransaccion.desde(this).
 */
public abstract class Transaccion {

    private static final AtomicLong CONTADOR_TRANSACCIONES = new AtomicLong(1);

    private Long id;
    private final String numeroTransaccion;
    private final BigDecimal monto;
    private final LocalDateTime fecha;
    private EstadoTransaccion estado;

    // null cuando el cliente la hizo directamente (banca en linea); solo se
    // asigna en el flujo de ventanilla (HU-007: "registrar el cajero
    // responsable de cada transaccion"). No es parte del constructor porque
    // la transaccion se construye antes de saberse si es de ventanilla.
    private Cajero cajero;

    // Saldo de las cuentas justo despues de aplicar(); null hasta que la
    // transaccion se completa. Destino solo aplica a transferencias.
    private BigDecimal saldoPosteriorOrigen;
    private BigDecimal saldoPosteriorDestino;

    protected Transaccion(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto de la transaccion debe ser positivo");
        }
        this.numeroTransaccion = "TRX-%06d".formatted(CONTADOR_TRANSACCIONES.getAndIncrement());
        this.monto = monto;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoTransaccion.PENDIENTE;
    }

    /**
     * El numeroTransaccion se genera con un AtomicLong en memoria que
     * reinicia en cada arranque de la JVM; sin esto, una transaccion nueva
     * tras un reinicio podria repetir el numero de una ya persistida en
     * transacciones.dat. CuentaServiceImpl la llama por cada
     * RegistroTransaccion cargado al arrancar.
     */
    public static void avanzarContador(String numeroTransaccion) {
        long valor = Long.parseLong(numeroTransaccion.substring(4));
        CONTADOR_TRANSACCIONES.updateAndGet(actual -> Math.max(actual, valor + 1));
    }

    public abstract boolean validarTransaccion();

    public abstract String generarComprobante();

    protected abstract void aplicar();

    /**
     * Hook que ejecutar() llama justo despues de aplicar(), todavia dentro de
     * la seccion critica del servicio: leer aqui el saldo garantiza que no
     * se cuele otro movimiento entre aplicar y la lectura. Cada subtipo dice
     * de que cuentas lo toma.
     */
    protected abstract void capturarSaldosPosteriores();

    protected final void fijarSaldosPosteriores(BigDecimal origen, BigDecimal destino) {
        this.saldoPosteriorOrigen = origen;
        this.saldoPosteriorDestino = destino;
    }

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
            capturarSaldosPosteriores();
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

    public BigDecimal getSaldoPosteriorOrigen() {
        return saldoPosteriorOrigen;
    }

    public BigDecimal getSaldoPosteriorDestino() {
        return saldoPosteriorDestino;
    }

    public void setCajero(Cajero cajero) {
        this.cajero = cajero;
    }

    public Optional<Cajero> getCajero() {
        return Optional.ofNullable(cajero);
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
