package com.banco.core.util;

import com.banco.core.model.entity.RegistroTransaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelloDigitalTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 20, 10, 0, 0);

    private RegistroTransaccion registro(String monto, String cajero) {
        return new RegistroTransaccion("TRX-000001", "Deposito", new BigDecimal(monto), FECHA,
                "COMPLETADA", "AHO-000001", null, cajero);
    }

    @Test
    void empiezaConElCodigoDelCajeroYTermina8CaracteresHex() {
        String sello = SelloDigital.generar(registro("50.00", "CAJ-000001"));

        assertTrue(sello.matches("CAJ-000001-[0-9A-F]{8}"), sello);
    }

    @Test
    void esDeterministaParaElMismoMovimiento() {
        assertEquals(SelloDigital.generar(registro("50.00", "CAJ-000001")),
                SelloDigital.generar(registro("50.00", "CAJ-000001")));
    }

    @Test
    void cambiarCualquierDatoCambiaElSello() {
        String base = SelloDigital.generar(registro("50.00", "CAJ-000001"));

        assertNotEquals(base, SelloDigital.generar(registro("50.01", "CAJ-000001")));
        assertNotEquals(base.substring(base.lastIndexOf('-')),
                SelloDigital.generar(registro("50.00", "CAJ-000002")).substring(
                        SelloDigital.generar(registro("50.00", "CAJ-000002")).lastIndexOf('-')));
    }

    @Test
    void sinCajeroNoHaySello() {
        assertNull(SelloDigital.generar(registro("50.00", null)));
    }
}
