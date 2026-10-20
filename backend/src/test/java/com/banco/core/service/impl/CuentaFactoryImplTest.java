package com.banco.core.service.impl;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.CuentaCorriente;
import com.banco.core.model.entity.CuentaPlazoFijo;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.model.entity.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CuentaFactoryImplTest {

    private final CuentaFactoryImpl fabrica = new CuentaFactoryImpl();
    private final Cliente titular =
            new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    private final BigDecimal saldo = new BigDecimal("100.00");

    @Test
    void creaCadaSubtipoSegunElTipo() {
        assertInstanceOf(CuentaAhorros.class, fabrica.crear(TipoCuenta.AHORROS, titular, saldo, null));
        assertInstanceOf(CuentaCorriente.class, fabrica.crear(TipoCuenta.CORRIENTE, titular, saldo, null));
        assertInstanceOf(CuentaPlazoFijo.class, fabrica.crear(TipoCuenta.PLAZO_FIJO, titular, new BigDecimal("500.00"), 6));
    }

    @Test
    void laCuentaCreadaQuedaAsociadaAlTitularYConElSaldoInicial() {
        Cuenta cuenta = fabrica.crear(TipoCuenta.AHORROS, titular, saldo, null);

        assertSame(titular, cuenta.getTitular());
        assertEquals(saldo, cuenta.getSaldo());
    }

    @Test
    void plazoFijoSinPlazoLanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> fabrica.crear(TipoCuenta.PLAZO_FIJO, titular, new BigDecimal("500.00"), null));
    }

    @Test
    void tipoNuloLanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.crear(null, titular, saldo, null));
    }

    @Test
    void sePuedeRegistrarUnCreadorSinTocarElServicio() {
        Cuenta fija = new CuentaCorriente(titular, saldo);
        fabrica.registrar(TipoCuenta.AHORROS, (t, s, p) -> fija);

        assertSame(fija, fabrica.crear(TipoCuenta.AHORROS, titular, saldo, null));
    }
}
