package com.banco.core.service.impl;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.CuentaRepository;
import com.banco.core.dao.TransaccionDAO;
import com.banco.core.model.dto.ReporteFinancieroDTO;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.CuentaCorriente;
import com.banco.core.model.entity.Deposito;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.Retiro;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteServiceImplTest {

    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final CuentaRepository cuentas = mock(CuentaRepository.class);
    private TransaccionDAO transacciones;
    private ReporteServiceImpl servicio;

    // TransaccionDAO es una clase concreta: se usa la real sobre un archivo temporal en vez de un mock.
    @BeforeEach
    void crearServicio() throws IOException {
        transacciones = new TransaccionDAO(Files.createTempDirectory("reporte-test").resolve("transacciones.dat"));
        servicio = new ReporteServiceImpl(clientes, cuentas, transacciones);
    }

    @Test
    void sumaSaldosAgrupaPorTipoYSoloCuentaMontosCompletados() {
        Cliente titular = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        Cuenta ahorros = new CuentaAhorros(titular, new BigDecimal("100.00"));
        Cuenta corriente = new CuentaCorriente(titular, new BigDecimal("200.00"));

        Deposito deposito = new Deposito(ahorros, new BigDecimal("50.00"));
        deposito.ejecutar();
        Retiro retiroRechazado = new Retiro(ahorros, new BigDecimal("9999.00"));
        try {
            retiroRechazado.ejecutar();
        } catch (RuntimeException esperado) {
            // queda RECHAZADA; es el caso que el reporte debe contar aparte
        }

        when(clientes.count()).thenReturn(1L);
        when(cuentas.findAll()).thenReturn(List.of(ahorros, corriente));
        transacciones.guardar(RegistroTransaccion.desde(deposito));
        transacciones.guardar(RegistroTransaccion.desde(retiroRechazado));

        ReporteFinancieroDTO reporte = servicio.generarReporteFinanciero();

        assertEquals(1, reporte.totalClientes());
        assertEquals(2, reporte.totalCuentas());
        assertEquals(1L, reporte.cuentasPorTipo().get("Cuenta de Ahorros"));
        assertEquals(0, new BigDecimal("350.00").compareTo(reporte.saldoTotal()));
        assertEquals(2, reporte.totalTransacciones());
        assertEquals(1, reporte.transaccionesRechazadas());
        assertEquals(0, new BigDecimal("50.00").compareTo(reporte.montoPorTipoTransaccion().get("Deposito")));
    }
}
