package com.banco.core.service.impl;

import com.banco.core.model.dto.HistorialResumenDTO;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoTransaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistorialServiceImplTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 20, 9, 0, 0);

    private final HistorialServiceImpl servicio = new HistorialServiceImpl();

    private RegistroTransaccion registro(String numero, String tipo, String monto, int minuto,
                                         String origen, String destino) {
        return new RegistroTransaccion(numero, tipo, new BigDecimal(monto), T0.plusMinutes(minuto),
                "COMPLETADA", origen, destino, null);
    }

    @Test
    void cuentaSinMovimientosDevuelveListaVacia() {
        assertTrue(servicio.listar("AHO-000001").isEmpty());
    }

    @Test
    void devuelveLosMovimientosEnOrdenCronologicoAunqueSeIndexenDesordenados() {
        servicio.indexar(registro("TRX-000002", "Retiro", "10.00", 5, "AHO-000001", null));
        servicio.indexar(registro("TRX-000001", "Deposito", "50.00", 1, "AHO-000001", null));

        List<RegistroTransaccion> historial = servicio.listar("AHO-000001");

        assertEquals("TRX-000001", historial.get(0).getNumeroTransaccion());
        assertEquals("TRX-000002", historial.get(1).getNumeroTransaccion());
    }

    @Test
    void unaTransferenciaApareceEnElHistorialDeOrigenYDestino() {
        servicio.indexar(registro("TRX-000003", "Transferencia", "20.00", 2, "AHO-000001", "CTE-000001"));

        assertEquals(1, servicio.listar("AHO-000001").size());
        assertEquals(1, servicio.listar("CTE-000001").size());
        assertTrue(servicio.listar("AHO-000999").isEmpty());
    }

    @Test
    void filtrarPorTipoDevuelveSoloEseTipoYNuloNoFiltra() {
        servicio.indexar(registro("TRX-000001", "Deposito", "50.00", 1, "AHO-000001", null));
        servicio.indexar(registro("TRX-000002", "Retiro", "10.00", 2, "AHO-000001", null));

        assertEquals(1, servicio.filtrar("AHO-000001", TipoTransaccion.RETIRO).size());
        assertEquals("TRX-000002", servicio.filtrar("AHO-000001", TipoTransaccion.RETIRO).get(0).getNumeroTransaccion());
        assertEquals(2, servicio.filtrar("AHO-000001", null).size());
        assertTrue(servicio.filtrar("AHO-000001", TipoTransaccion.TRANSFERENCIA).isEmpty());
    }

    @Test
    void resumenSumaDepositosComoIngresosYRetirosYTransferenciasComoEgresos() {
        servicio.indexar(registro("TRX-000001", "Deposito", "100.00", 1, "AHO-000001", null));
        servicio.indexar(registro("TRX-000002", "Deposito", "50.00", 2, "AHO-000001", null));
        servicio.indexar(registro("TRX-000003", "Retiro", "30.00", 3, "AHO-000001", null));
        servicio.indexar(registro("TRX-000004", "Transferencia", "20.00", 4, "AHO-000001", "CTE-000001"));

        HistorialResumenDTO resumen = servicio.resumir("AHO-000001");

        assertEquals(new BigDecimal("150.00"), resumen.ingresos());
        assertEquals(new BigDecimal("50.00"), resumen.egresos());
    }

    @Test
    void resumenDeCuentaSinMovimientosEsCero() {
        HistorialResumenDTO resumen = servicio.resumir("AHO-000001");

        assertEquals(BigDecimal.ZERO, resumen.ingresos());
        assertEquals(BigDecimal.ZERO, resumen.egresos());
    }

    @Test
    void laListaDevueltaEsUnaCopiaInmutable() {
        servicio.indexar(registro("TRX-000001", "Deposito", "50.00", 1, "AHO-000001", null));

        List<RegistroTransaccion> copia = servicio.listar("AHO-000001");

        assertThrows(UnsupportedOperationException.class, () -> copia.add(copia.get(0)));
    }
}
