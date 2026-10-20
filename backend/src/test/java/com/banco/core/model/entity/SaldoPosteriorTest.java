package com.banco.core.model.entity;

import com.banco.core.dao.TransaccionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaldoPosteriorTest {

    private final Cliente titular =
            new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

    private Cuenta cuenta(String saldo) {
        return new CuentaAhorros(titular, new BigDecimal(saldo));
    }

    @Test
    void depositoGuardaElSaldoDespuesDeAplicar() {
        Cuenta cuenta = cuenta("100.00");
        Transaccion deposito = new Deposito(cuenta, new BigDecimal("50.00"));

        deposito.ejecutar();

        assertEquals(new BigDecimal("150.00"), deposito.getSaldoPosteriorOrigen());
        assertNull(deposito.getSaldoPosteriorDestino());
    }

    @Test
    void retiroGuardaElSaldoDespuesDeAplicar() {
        Cuenta cuenta = cuenta("100.00");
        Transaccion retiro = new Retiro(cuenta, new BigDecimal("30.00"));

        retiro.ejecutar();

        assertEquals(new BigDecimal("70.00"), retiro.getSaldoPosteriorOrigen());
    }

    @Test
    void transferenciaGuardaElSaldoDeOrigenYDeDestino() {
        Cuenta origen = cuenta("100.00");
        Cuenta destino = cuenta("30.00");
        Transaccion transferencia = new Transferencia(origen, destino, new BigDecimal("40.00"));

        transferencia.ejecutar();

        assertEquals(new BigDecimal("60.00"), transferencia.getSaldoPosteriorOrigen());
        assertEquals(new BigDecimal("70.00"), transferencia.getSaldoPosteriorDestino());
    }

    @Test
    void unaTransaccionRechazadaNoDejaSaldoPosterior() {
        Cuenta cuenta = cuenta("30.00");
        Transaccion retiro = new Retiro(cuenta, new BigDecimal("500.00"));

        assertThrows(RuntimeException.class, retiro::ejecutar);

        assertNull(retiro.getSaldoPosteriorOrigen());
    }

    @Test
    void registroElijeElSaldoSegunLaCuentaQueSeMira() {
        Cuenta origen = cuenta("100.00");
        Cuenta destino = cuenta("30.00");
        Transaccion transferencia = new Transferencia(origen, destino, new BigDecimal("40.00"));
        transferencia.ejecutar();

        RegistroTransaccion registro = RegistroTransaccion.desde(transferencia);

        assertEquals(new BigDecimal("60.00"), registro.saldoPosteriorDe(origen.getNumeroCuenta()));
        assertEquals(new BigDecimal("70.00"), registro.saldoPosteriorDe(destino.getNumeroCuenta()));
    }

    @Test
    void registroSinSaldosComoLosDeUnDatViejoDevuelveNull() {
        var viejo = new RegistroTransaccion("TRX-000001", "Deposito", BigDecimal.TEN, LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", null, null);

        assertNull(viejo.saldoPosteriorDe("AHO-000001"));
    }

    @Test
    void losSaldosSobrevivenAlGuardarYLeerElDat(@TempDir Path dir) {
        TransaccionDAO dao = new TransaccionDAO(dir.resolve("t.dat"));
        Transaccion deposito = new Deposito(cuenta("100.00"), new BigDecimal("50.00"));
        deposito.ejecutar();

        dao.guardar(RegistroTransaccion.desde(deposito));

        RegistroTransaccion leido = dao.listarTodos().get(0);
        assertEquals(new BigDecimal("150.00"), leido.saldoPosteriorDe(leido.getNumeroCuentaOrigen()));
    }
}
