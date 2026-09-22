package com.banco.core.service.impl;

import com.banco.core.exception.ClienteNoEncontradoException;
import com.banco.core.exception.SaldoInsuficienteException;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.Notificacion;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Transactional en la clase: cada @Test corre en su propia transaccion,
 * revertida al final -aisla los datos entre tests sin limpiar tablas a
 * mano-. numeroCuenta/numeroTransaccion siguen siendo unicos entre tests
 * de todas formas (AtomicLong no se resetea), asi que el historial en
 * memoria de cada cuenta no se pisa entre tests aunque la fila en Postgres
 * ya se haya revertido.
 */
@Testcontainers
@SpringBootTest
@Import(TransaccionDaoTestConfig.class)
@Transactional
class CuentaServiceImplTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ClienteServiceImpl clienteService;

    @Autowired
    private CuentaServiceImpl cuentaService;

    @BeforeEach
    void registrarClientePrueba() {
        clienteService.registrar("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        // notificaciones (Queue en memoria, no cubierta por el rollback de
        // @Transactional) vive en el mismo bean singleton entre tests de
        // esta clase: se drena antes de cada test para que no arrastre las
        // notificaciones generadas por los tests anteriores.
        cuentaService.obtenerNotificacionesPendientes();
    }

    @Test
    void abrirCuentaAhorrosQuedaConsultablePorNumero() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);

        assertEquals("Cuenta de Ahorros", cuenta.getTipoCuenta());
        assertTrue(cuentaService.buscarPorNumero(cuenta.getNumeroCuenta()).isPresent());
        assertEquals(1, cuentaService.listarPorCliente("12345678-9").size());
    }

    @Test
    void abrirCuentaConClienteInexistenteLanzaClienteNoEncontrado() {
        assertThrows(ClienteNoEncontradoException.class, () ->
                cuentaService.abrirCuenta("00000000-0", "AHORROS", new BigDecimal("100.00"), null));
    }

    @Test
    void abrirCuentaPlazoFijoSinPlazoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                cuentaService.abrirCuenta("12345678-9", "PLAZO_FIJO", new BigDecimal("500.00"), null));
    }

    @Test
    void depositarActualizaSaldoYQuedaEnElHistorial() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);

        RegistroTransaccion deposito = cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), cuenta.getSaldo());
        assertEquals(1, cuentaService.historial(cuenta.getNumeroCuenta()).size());
        assertEquals(deposito, cuentaService.historial(cuenta.getNumeroCuenta()).get(0));
    }

    @Test
    void retirarConSaldoInsuficienteLanzaExcepcionYNoQuedaEnHistorial() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);

        assertThrows(SaldoInsuficienteException.class, () ->
                cuentaService.retirar(cuenta.getNumeroCuenta(), new BigDecimal("200.00")));
        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
        assertTrue(cuentaService.historial(cuenta.getNumeroCuenta()).isEmpty());
    }

    @Test
    void transferirActualizaAmbasCuentasYApareceEnAmbosHistoriales() {
        Cuenta origen = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);
        Cuenta destino = cuentaService.abrirCuenta("12345678-9", "CORRIENTE", new BigDecimal("100.00"), null);

        cuentaService.transferir(origen.getNumeroCuenta(), destino.getNumeroCuenta(), new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), origen.getSaldo());
        assertEquals(new BigDecimal("130.00"), destino.getSaldo());
        assertEquals(1, cuentaService.historial(origen.getNumeroCuenta()).size());
        assertEquals(1, cuentaService.historial(destino.getNumeroCuenta()).size());
    }

    @Test
    void historialDevuelveLasTransaccionesEnOrdenCronologico() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "CORRIENTE", new BigDecimal("100.00"), null);

        RegistroTransaccion primero = cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("10.00"));
        RegistroTransaccion segundo = cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("20.00"));
        RegistroTransaccion tercero = cuentaService.retirar(cuenta.getNumeroCuenta(), new BigDecimal("5.00"));

        List<RegistroTransaccion> historial = cuentaService.historial(cuenta.getNumeroCuenta());

        assertEquals(List.of(primero, segundo, tercero), historial);
    }

    @Test
    void depositarEnVentanillaDejaRegistradoElCajeroResponsable() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);
        Cajero cajero = new Cajero("98765432-1", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", "clave123");

        RegistroTransaccion deposito = cuentaService.depositarEnVentanilla(cuenta.getNumeroCuenta(),
                new BigDecimal("50.00"), cajero);

        assertEquals(new BigDecimal("150.00"), cuenta.getSaldo());
        assertEquals(cajero.getCodigoEmpleado(), deposito.getCodigoEmpleadoCajero());
    }

    @Test
    void depositarPorBancaEnLineaNoQuedaAsociadoAUnCajero() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);

        RegistroTransaccion deposito = cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("50.00"));

        assertNull(deposito.getCodigoEmpleadoCajero());
    }

    @Test
    void ventanillaSinCajeroLanzaExcepcion() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);

        assertThrows(IllegalArgumentException.class, () ->
                cuentaService.depositarEnVentanilla(cuenta.getNumeroCuenta(), new BigDecimal("50.00"), null));
    }

    @Test
    void aplicarInteresPeriodicoCapitalizaSoloCuentasDeAhorro() {
        Cuenta ahorros = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);
        Cuenta corriente = cuentaService.abrirCuenta("12345678-9", "CORRIENTE", new BigDecimal("100.00"), null);

        cuentaService.aplicarInteresPeriodico();

        assertTrue(ahorros.getSaldo().compareTo(new BigDecimal("100.00")) > 0);
        assertEquals(new BigDecimal("100.00"), corriente.getSaldo());
        assertEquals(1, cuentaService.historial(ahorros.getNumeroCuenta()).size());
        assertTrue(cuentaService.historial(corriente.getNumeroCuenta()).isEmpty());
    }

    @Test
    void aplicarInteresPeriodicoIgnoraCuentaBloqueadaSinFallar() {
        Cuenta ahorros = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);
        ahorros.bloquear();

        cuentaService.aplicarInteresPeriodico();

        assertEquals(new BigDecimal("100.00"), ahorros.getSaldo());
    }

    @Test
    void obtenerNotificacionesPendientesDrenaEnOrdenFifo() {
        Cuenta cuenta = cuentaService.abrirCuenta("12345678-9", "AHORROS", new BigDecimal("100.00"), null);

        cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("10.00"));
        cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("20.00"));

        List<Notificacion> pendientes = cuentaService.obtenerNotificacionesPendientes();

        assertEquals(2, pendientes.size());
        assertTrue(pendientes.get(0).fecha().compareTo(pendientes.get(1).fecha()) <= 0);
        assertTrue(cuentaService.obtenerNotificacionesPendientes().isEmpty());
    }
}
