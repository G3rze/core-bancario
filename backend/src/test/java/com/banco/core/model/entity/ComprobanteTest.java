package com.banco.core.model.entity;

import com.banco.core.dao.TransaccionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComprobanteTest {

    private final Cliente titular =
            new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

    @Test
    void elRegistroGuardaElComprobanteDeLaTransaccionEjecutada() {
        Cuenta cuenta = new CuentaAhorros(titular, new BigDecimal("100.00"));
        Transaccion deposito = new Deposito(cuenta, new BigDecimal("50.00"));
        deposito.ejecutar();

        RegistroTransaccion registro = RegistroTransaccion.desde(deposito);

        assertEquals(deposito.generarComprobante(), registro.getComprobante());
        assertTrue(registro.getComprobante().contains("COMPLETADA"));
    }

    @Test
    void cadaTipoGeneraSuPropioComprobante() {
        Cuenta origen = new CuentaAhorros(titular, new BigDecimal("100.00"));
        Cuenta destino = new CuentaCorriente(titular, new BigDecimal("100.00"));
        Transaccion retiro = new Retiro(origen, new BigDecimal("10.00"));
        Transaccion transferencia = new Transferencia(origen, destino, new BigDecimal("20.00"));
        retiro.ejecutar();
        transferencia.ejecutar();

        assertTrue(RegistroTransaccion.desde(retiro).getComprobante().startsWith("Retiro"));
        String texto = RegistroTransaccion.desde(transferencia).getComprobante();
        assertTrue(texto.startsWith("Transferencia"));
        assertTrue(texto.contains(origen.getNumeroCuenta()) && texto.contains(destino.getNumeroCuenta()));
    }

    @Test
    void registroAnteriorAlCampoNoTieneComprobante() {
        var viejo = new RegistroTransaccion("TRX-000001", "Deposito", BigDecimal.TEN, LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", null, null);

        assertNull(viejo.getComprobante());
    }

    @Test
    void elComprobanteSobreviveAlDat(@TempDir Path dir) {
        TransaccionDAO dao = new TransaccionDAO(dir.resolve("t.dat"));
        Transaccion deposito = new Deposito(new CuentaAhorros(titular, new BigDecimal("100.00")), new BigDecimal("50.00"));
        deposito.ejecutar();

        dao.guardar(RegistroTransaccion.desde(deposito));

        assertEquals(deposito.generarComprobante(), dao.listarTodos().get(0).getComprobante());
    }

    @Test
    void comprobanteDeAperturaIncluyeTipoNumeroTitularYSaldo() {
        Cuenta ahorros = new CuentaAhorros(titular, new BigDecimal("100.00"));
        Cuenta corriente = new CuentaCorriente(titular, new BigDecimal("200.00"));

        String texto = ahorros.generarComprobanteApertura();

        assertTrue(texto.contains(ahorros.getNumeroCuenta()));
        assertTrue(texto.contains("Ana Lopez"));
        assertTrue(texto.contains("100.00"));
        assertTrue(texto.startsWith("Apertura " + ahorros.getTipoCuenta()));
        assertTrue(corriente.generarComprobanteApertura().startsWith("Apertura " + corriente.getTipoCuenta()));
    }
}
