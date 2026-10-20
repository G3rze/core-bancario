package com.banco.core.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Lo unico que TransaccionDAO persiste a .dat: un log plano, sin
 * referencias a entidades JPA (Cuenta/Cliente/Cajero viven en Postgres
 * ahora). Serializar una entidad Hibernate fuera de una sesion activa
 * arriesga LazyInitializationException o proxies que no deserializan bien
 * -por eso Transaccion (el objeto de negocio, con referencias vivas a
 * Cuenta/Cajero, usado solo durante ejecutar()) nunca se persiste
 * directamente; se convierte a este record plano justo antes de guardarse.
 */
public class RegistroTransaccion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private final String numeroTransaccion;
    private final String tipo;
    private final BigDecimal monto;
    private final LocalDateTime fecha;
    private final String estado;
    private final String numeroCuentaOrigen;
    // null salvo en Transferencia.
    private final String numeroCuentaDestino;
    // null salvo en transacciones procesadas en ventanilla.
    private final String codigoEmpleadoCajero;
    // Se agregaron despues de que ya existian .dat: Java deserializa un campo
    // ausente en el archivo como null, asi que los registros viejos quedan
    // sin saldo posterior (no se inventa uno) y siguen leyendose.
    private final BigDecimal saldoPosteriorOrigen;
    private final BigDecimal saldoPosteriorDestino;

    /** Registro sin saldos posteriores (equivale a uno leido de un .dat anterior). */
    public RegistroTransaccion(String numeroTransaccion, String tipo, BigDecimal monto, LocalDateTime fecha,
                                String estado, String numeroCuentaOrigen, String numeroCuentaDestino,
                                String codigoEmpleadoCajero) {
        this(numeroTransaccion, tipo, monto, fecha, estado, numeroCuentaOrigen, numeroCuentaDestino,
                codigoEmpleadoCajero, null, null);
    }

    public RegistroTransaccion(String numeroTransaccion, String tipo, BigDecimal monto, LocalDateTime fecha,
                                String estado, String numeroCuentaOrigen, String numeroCuentaDestino,
                                String codigoEmpleadoCajero, BigDecimal saldoPosteriorOrigen,
                                BigDecimal saldoPosteriorDestino) {
        this.numeroTransaccion = numeroTransaccion;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.estado = estado;
        this.numeroCuentaOrigen = numeroCuentaOrigen;
        this.numeroCuentaDestino = numeroCuentaDestino;
        this.codigoEmpleadoCajero = codigoEmpleadoCajero;
        this.saldoPosteriorOrigen = saldoPosteriorOrigen;
        this.saldoPosteriorDestino = saldoPosteriorDestino;
    }

    public static RegistroTransaccion desde(Transaccion transaccion) {
        String origen;
        String destino = null;
        TipoTransaccion tipo;
        if (transaccion instanceof Transferencia transferencia) {
            origen = transferencia.getOrigen().getNumeroCuenta();
            destino = transferencia.getDestino().getNumeroCuenta();
            tipo = TipoTransaccion.TRANSFERENCIA;
        } else if (transaccion instanceof Deposito deposito) {
            origen = deposito.getCuenta().getNumeroCuenta();
            tipo = TipoTransaccion.DEPOSITO;
        } else if (transaccion instanceof Retiro retiro) {
            origen = retiro.getCuenta().getNumeroCuenta();
            tipo = TipoTransaccion.RETIRO;
        } else {
            throw new IllegalStateException("Tipo de transaccion desconocido: " + transaccion.getClass());
        }
        return new RegistroTransaccion(
                transaccion.getNumeroTransaccion(),
                tipo.etiqueta(),
                transaccion.getMonto(),
                transaccion.getFecha(),
                transaccion.getEstado().name(),
                origen,
                destino,
                transaccion.getCajero().map(Cajero::getCodigoEmpleado).orElse(null),
                transaccion.getSaldoPosteriorOrigen(),
                transaccion.getSaldoPosteriorDestino());
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

    public String getTipo() {
        return tipo;
    }

    /**
     * El campo persistido sigue siendo el String (compatibilidad con los
     * .dat ya escritos); el enum se deriva de el al consultarlo.
     */
    public TipoTransaccion getTipoTransaccion() {
        return TipoTransaccion.desde(tipo);
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getNumeroCuentaOrigen() {
        return numeroCuentaOrigen;
    }

    public String getNumeroCuentaDestino() {
        return numeroCuentaDestino;
    }

    public String getCodigoEmpleadoCajero() {
        return codigoEmpleadoCajero;
    }

    /**
     * Saldo de {@code numeroCuenta} despues de este movimiento. En una
     * transferencia el destino ve su propio saldo y cualquier otra cuenta el
     * del origen. null en registros anteriores a este campo.
     */
    public BigDecimal saldoPosteriorDe(String numeroCuenta) {
        if (numeroCuentaDestino != null && numeroCuentaDestino.equals(numeroCuenta)) {
            return saldoPosteriorDestino;
        }
        return saldoPosteriorOrigen;
    }

    /** Numeros de cuenta que este registro involucra (para indexar el historial por cuenta). */
    public List<String> numerosCuentaInvolucradas() {
        return numeroCuentaDestino == null
                ? List.of(numeroCuentaOrigen)
                : List.of(numeroCuentaOrigen, numeroCuentaDestino);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegistroTransaccion otro)) {
            return false;
        }
        return Objects.equals(numeroTransaccion, otro.numeroTransaccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroTransaccion);
    }

    @Override
    public String toString() {
        return "RegistroTransaccion{numeroTransaccion='%s', tipo='%s', monto=%s, estado='%s'}".formatted(
                numeroTransaccion, tipo, monto, estado);
    }
}
