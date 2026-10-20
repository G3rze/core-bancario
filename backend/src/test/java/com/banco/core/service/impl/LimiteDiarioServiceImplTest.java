package com.banco.core.service.impl;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimiteDiarioServiceImplTest {

    private final LimiteDiarioServiceImpl servicio = new LimiteDiarioServiceImpl();
    private final Cliente titular =
            new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

    private Cuenta cuenta() {
        return new CuentaAhorros(titular, new BigDecimal("100.00"));
    }

    @Test
    void permiteMontosHastaElLimiteDiarioInclusive() {
        Cuenta cuenta = cuenta();
        BigDecimal limite = cuenta.getLimiteRetiroDiario();

        assertDoesNotThrow(() -> servicio.validar(cuenta, limite));
    }

    @Test
    void rechazaUnMontoQueSuperaElLimite() {
        Cuenta cuenta = cuenta();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.validar(cuenta, cuenta.getLimiteRetiroDiario().add(BigDecimal.ONE)));
    }

    @Test
    void elAcumuladoDelDiaCuentaParaLaSiguienteValidacion() {
        Cuenta cuenta = cuenta();
        BigDecimal limite = cuenta.getLimiteRetiroDiario();
        BigDecimal mitad = limite.divide(new BigDecimal("2"));

        servicio.validar(cuenta, mitad);
        servicio.registrar(cuenta, mitad);

        assertDoesNotThrow(() -> servicio.validar(cuenta, mitad));
        assertThrows(IllegalArgumentException.class, () -> servicio.validar(cuenta, mitad.add(BigDecimal.ONE)));
    }

    @Test
    void validarSinRegistrarNoAcumula() {
        Cuenta cuenta = cuenta();
        BigDecimal limite = cuenta.getLimiteRetiroDiario();

        servicio.validar(cuenta, limite);

        assertDoesNotThrow(() -> servicio.validar(cuenta, limite));
    }

    @Test
    void cadaCuentaTieneSuPropioAcumulado() {
        Cuenta a = cuenta();
        Cuenta b = cuenta();
        servicio.registrar(a, a.getLimiteRetiroDiario());

        assertThrows(IllegalArgumentException.class, () -> servicio.validar(a, BigDecimal.ONE));
        assertDoesNotThrow(() -> servicio.validar(b, BigDecimal.ONE));
    }
}
