package com.banco.core.model.entity;

import com.banco.core.exception.SaldoInsuficienteException;
import com.banco.core.exception.TransaccionInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransaccionTest {

    private Cliente crearClientePrueba() {
        return new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    }

    @Test
    void rechazaMontoNoPositivoEnElConstructor() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> new Deposito(cuenta, BigDecimal.ZERO));
    }

    @Test
    void depositoValidoQuedaCompletadoYAumentaSaldo() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));
        Transaccion deposito = new Deposito(cuenta, new BigDecimal("50.00"));

        deposito.ejecutar();

        assertEquals(EstadoTransaccion.COMPLETADA, deposito.getEstado());
        assertEquals(new BigDecimal("150.00"), cuenta.getSaldo());
    }

    @Test
    void retiroValidoQuedaCompletadoYDisminuyeSaldo() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));
        Transaccion retiro = new Retiro(cuenta, new BigDecimal("40.00"));

        retiro.ejecutar();

        assertEquals(EstadoTransaccion.COMPLETADA, retiro.getEstado());
        assertEquals(new BigDecimal("60.00"), cuenta.getSaldo());
    }

    @Test
    void retiroSinFondosQuedaRechazadoYLanzaSaldoInsuficiente() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));
        Transaccion retiro = new Retiro(cuenta, new BigDecimal("500.00"));

        assertThrows(SaldoInsuficienteException.class, retiro::ejecutar);
        assertEquals(EstadoTransaccion.RECHAZADA, retiro.getEstado());
    }

    @Test
    void transferenciaMueveFondosEntreCuentas() {
        Cliente cliente = crearClientePrueba();
        Cuenta origen = new CuentaAhorros(cliente, new BigDecimal("100.00"));
        Cuenta destino = new CuentaAhorros(cliente, new BigDecimal("25.00"));
        Transaccion transferencia = new Transferencia(origen, destino, new BigDecimal("30.00"));

        transferencia.ejecutar();

        assertEquals(new BigDecimal("70.00"), origen.getSaldo());
        assertEquals(new BigDecimal("55.00"), destino.getSaldo());
    }

    @Test
    void transferenciaHaciaLaMismaCuentaEsInvalida() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));
        Transaccion transferencia = new Transferencia(cuenta, cuenta, new BigDecimal("10.00"));

        assertThrows(TransaccionInvalidaException.class, transferencia::ejecutar);
        assertEquals(EstadoTransaccion.RECHAZADA, transferencia.getEstado());
    }

    @Test
    void depositoEnCuentaPlazoFijoQuedaRechazadoComoTransaccionInvalida() {
        Cuenta cuenta = new CuentaPlazoFijo(crearClientePrueba(), new BigDecimal("1000.00"), 6);
        Transaccion deposito = new Deposito(cuenta, new BigDecimal("50.00"));

        assertThrows(TransaccionInvalidaException.class, deposito::ejecutar);
        assertEquals(EstadoTransaccion.RECHAZADA, deposito.getEstado());
    }

    @Test
    void generarComprobanteIncluyeNumeroYMonto() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));
        Transaccion deposito = new Deposito(cuenta, new BigDecimal("50.00"));

        String comprobante = deposito.generarComprobante();

        assertTrue(comprobante.contains(deposito.getNumeroTransaccion()));
        assertTrue(comprobante.contains("50.00"));
    }
}
