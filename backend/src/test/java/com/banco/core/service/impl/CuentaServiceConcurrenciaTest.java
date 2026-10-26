package com.banco.core.service.impl;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.CuentaRepository;
import com.banco.core.exception.SaldoInsuficienteException;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.model.entity.TipoCuenta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Sin @Transactional en la clase: cada hilo abre su propia transaccion, como
 * dos requests HTTP simultaneas. Si el bloqueo de fila fallara, los retiros
 * leerian el mismo saldo y se perderia alguna actualizacion.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TransaccionDaoTestConfig.class)
class CuentaServiceConcurrenciaTest {

    @Autowired
    private ClienteServiceImpl clienteService;

    @Autowired
    private CuentaServiceImpl cuentaService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Este test confirma datos de verdad (sin rollback): se limpian para no
    // aparecer en listados de otros tests que comparten la misma base H2.
    @AfterEach
    void limpiar() {
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void retirosConcurrentesNuncaDejanElSaldoNegativoNiPierdenActualizaciones() throws Exception {
        clienteService.registrar("99999999-9", "Prueba Concurrente", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        Cuenta cuenta = cuentaService.abrirCuenta("99999999-9", TipoCuenta.AHORROS, new BigDecimal("100.00"), null);
        String numero = cuenta.getNumeroCuenta();

        int hilos = 20;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitosos = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();
        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            tareas.add(pool.submit(() -> {
                salida.await();
                try {
                    cuentaService.retirar(numero, new BigDecimal("10.00"));
                    exitosos.incrementAndGet();
                } catch (SaldoInsuficienteException e) {
                    rechazados.incrementAndGet();
                }
                return null;
            }));
        }
        salida.countDown();
        for (Future<?> tarea : tareas) {
            tarea.get();
        }
        pool.shutdown();

        // 100 de saldo y 20 retiros de 10: exactamente 10 pasan y el saldo queda en 0.
        // Sin bloqueo de fila, varios leerian el mismo saldo y pasarian de mas.
        assertEquals(10, exitosos.get());
        assertEquals(10, rechazados.get());
        assertEquals(0, BigDecimal.ZERO.compareTo(cuentaService.buscarPorNumero(numero).orElseThrow().getSaldo()));
    }
}
