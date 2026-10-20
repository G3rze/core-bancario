package com.banco.core.util;

import com.banco.core.model.entity.RegistroTransaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorHistorialTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 19, 14, 30, 5);

    private RegistroTransaccion deposito(BigDecimal saldoPosterior) {
        return new RegistroTransaccion("TRX-000001", "Deposito", new BigDecimal("50.00"), FECHA,
                "COMPLETADA", "AHO-000001", null, null, saldoPosterior, null);
    }

    @Test
    void generaEncabezadoYUnaLineaPorMovimiento() {
        String texto = ExportadorHistorial.aTexto("AHO-000001", List.of(deposito(new BigDecimal("150.00"))));

        String[] lineas = texto.split("\n");
        assertEquals("Historial de la cuenta AHO-000001", lineas[0]);
        assertEquals("Movimientos: 1", lineas[1]);
        assertEquals("2026-10-19 14:30:05 | TRX-000001 | Deposito | 50.00 | 150.00 | COMPLETADA", lineas[3]);
    }

    @Test
    void registroSinSaldoPosteriorMuestraGuion() {
        String texto = ExportadorHistorial.aTexto("AHO-000001", List.of(deposito(null)));

        assertTrue(texto.contains("| 50.00 | - | COMPLETADA"));
    }

    @Test
    void historialVacioSoloTraeEncabezado() {
        String texto = ExportadorHistorial.aTexto("AHO-000001", List.of());

        assertTrue(texto.contains("Movimientos: 0"));
        assertEquals(3, texto.split("\n").length);
    }

    @Test
    void unaTransferenciaRecibidaMuestraElSaldoDelDestino() {
        var transferencia = new RegistroTransaccion("TRX-000002", "Transferencia", new BigDecimal("40.00"), FECHA,
                "COMPLETADA", "AHO-000001", "AHO-000002", null, new BigDecimal("60.00"), new BigDecimal("70.00"));

        assertTrue(ExportadorHistorial.aTexto("AHO-000002", List.of(transferencia)).contains("| 70.00 |"));
        assertTrue(ExportadorHistorial.aTexto("AHO-000001", List.of(transferencia)).contains("| 60.00 |"));
    }
}
