package com.banco.core.model.dto;

import com.banco.core.model.entity.RegistroTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * {@code comprobante} es el texto de Transaccion.generarComprobante(); null en
 * movimientos anteriores a que se guardara.
 * {@code saldoPosterior} es el saldo de la cuenta desde la que se mira el
 * movimiento; null en movimientos anteriores a que se guardara este dato.
 */
public record TransaccionDTO(
        String numeroTransaccion,
        String tipo,
        BigDecimal monto,
        LocalDateTime fecha,
        String estado,
        String codigoEmpleadoCajero,
        BigDecimal saldoPosterior,
        String comprobante) {

    /** Respuesta de una operacion: se mira desde la cuenta de origen. */
    public static TransaccionDTO desde(RegistroTransaccion registro) {
        return desde(registro, registro.getNumeroCuentaOrigen());
    }

    /** Historial de {@code numeroCuenta}: en una transferencia recibida, el saldo es el del destino. */
    public static TransaccionDTO desde(RegistroTransaccion registro, String numeroCuenta) {
        return new TransaccionDTO(
                registro.getNumeroTransaccion(),
                registro.getTipo(),
                registro.getMonto(),
                registro.getFecha(),
                registro.getEstado(),
                registro.getCodigoEmpleadoCajero(),
                registro.saldoPosteriorDe(numeroCuenta),
                registro.getComprobante());
    }
}
