package com.banco.core.model.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CuentaTest {

    private Cliente crearClientePrueba() {
        return new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    }

    @Test
    void rechazaAperturaBajoElMontoMinimo() {
        Cliente cliente = crearClientePrueba();

        assertThrows(IllegalArgumentException.class, () ->
                new CuentaAhorros(cliente, new BigDecimal("10.00")));
    }

    @Test
    void rechazaDepositoNegativo() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> cuenta.depositar(new BigDecimal("-5.00")));
    }

    @Test
    void rechazaRetiroNegativo() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> cuenta.retirar(new BigDecimal("-5.00")));
    }

    @Test
    void cuentaAhorrosCalculaInteresMensualEsperado() {
        Cuenta cuenta = new CuentaAhorros(crearClientePrueba(), new BigDecimal("1200.00"));

        // 1200 * 0.02 / 12 = 2.00
        assertEquals(new BigDecimal("2.00"), cuenta.calcularInteres());
    }

    @Test
    void cuentaCorrienteNoGeneraInteres() {
        Cuenta cuenta = new CuentaCorriente(crearClientePrueba(), new BigDecimal("500.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(cuenta.calcularInteres()));
    }

    @Test
    void cuentaCorrientePermiteSobregiroHastaElLimite() {
        CuentaCorriente cuenta = new CuentaCorriente(crearClientePrueba(), new BigDecimal("100.00"));

        cuenta.retirar(new BigDecimal("300.00"));

        assertEquals(new BigDecimal("-200.00"), cuenta.getSaldo());
    }

    @Test
    void cuentaCorrienteRechazaRetiroMasAllaDelSobregiro() {
        CuentaCorriente cuenta = new CuentaCorriente(crearClientePrueba(), new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> cuenta.retirar(new BigDecimal("301.00")));
    }

    @Test
    void cuentaPlazoFijoCalculaInteresDelPlazoCompleto() {
        Cuenta cuenta = new CuentaPlazoFijo(crearClientePrueba(), new BigDecimal("1000.00"), 12);

        // 1000 * 0.06 * 12 / 12 = 60.00
        assertEquals(new BigDecimal("60.00"), cuenta.calcularInteres());
    }

    @Test
    void cuentaPlazoFijoBloqueaDepositosAdicionales() {
        Cuenta cuenta = new CuentaPlazoFijo(crearClientePrueba(), new BigDecimal("1000.00"), 6);

        assertThrows(UnsupportedOperationException.class, () -> cuenta.depositar(new BigDecimal("50.00")));
    }

    @Test
    void cuentaPlazoFijoBloqueaRetirosAntesDelVencimiento() {
        Cuenta cuenta = new CuentaPlazoFijo(crearClientePrueba(), new BigDecimal("1000.00"), 6);

        assertThrows(UnsupportedOperationException.class, () -> cuenta.retirar(new BigDecimal("100.00")));
    }

    @Test
    void cuentaPlazoFijoRechazaPlazoNoPermitido() {
        Cliente cliente = crearClientePrueba();

        assertThrows(IllegalArgumentException.class, () ->
                new CuentaPlazoFijo(cliente, new BigDecimal("1000.00"), 5));
    }
}
