package com.banco.core.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TiposEnumTest {

    @Test
    void tipoCuentaSeParseaSinDistinguirMayusculas() {
        assertEquals(TipoCuenta.AHORROS, TipoCuenta.desde("ahorros"));
        assertEquals(TipoCuenta.PLAZO_FIJO, TipoCuenta.desde(" Plazo_Fijo "));
    }

    @Test
    void tipoCuentaNuloVacioODesconocidoLanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> TipoCuenta.desde(null));
        assertThrows(IllegalArgumentException.class, () -> TipoCuenta.desde(" "));
        assertThrows(IllegalArgumentException.class, () -> TipoCuenta.desde("VIP"));
    }

    @Test
    void tipoTransaccionAceptaNombreOEtiqueta() {
        assertEquals(TipoTransaccion.RETIRO, TipoTransaccion.desde("RETIRO"));
        assertEquals(TipoTransaccion.RETIRO, TipoTransaccion.desde("retiro"));
        assertEquals(TipoTransaccion.DEPOSITO, TipoTransaccion.desde("Deposito"));
        assertThrows(IllegalArgumentException.class, () -> TipoTransaccion.desde("Giro"));
        assertThrows(IllegalArgumentException.class, () -> TipoTransaccion.desde(null));
    }

    @Test
    void lasEtiquetasCoincidenConLoQueYaEstaEnLosDat() {
        // Si cambian, los historiales .dat existentes dejan de leerse.
        assertEquals("Deposito", TipoTransaccion.DEPOSITO.etiqueta());
        assertEquals("Retiro", TipoTransaccion.RETIRO.etiqueta());
        assertEquals("Transferencia", TipoTransaccion.TRANSFERENCIA.etiqueta());
    }

    @Test
    void soloElDepositoEsIngreso() {
        assertTrue(TipoTransaccion.DEPOSITO.esIngreso());
        assertFalse(TipoTransaccion.RETIRO.esIngreso());
        assertFalse(TipoTransaccion.TRANSFERENCIA.esIngreso());
    }

    @Test
    void registroDerivaElEnumDelTextoPersistido() {
        var registro = new RegistroTransaccion("T-1", "Transferencia", java.math.BigDecimal.ONE,
                java.time.LocalDateTime.now(), "EJECUTADA", "A", "B", null);

        assertEquals(TipoTransaccion.TRANSFERENCIA, registro.getTipoTransaccion());
    }
}
