package com.banco.core.config;

import com.banco.core.service.ClienteService;
import com.banco.core.service.CuentaService;
import com.banco.core.service.impl.TransaccionDaoTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Los datos demo se cargan con DEMO_DATA=true y no se duplican si el runner corre otra vez. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TransaccionDaoTestConfig.class)
@TestPropertySource(properties = "banco.demo.enabled=true")
class DemoDataRunnerTest {

    @Autowired
    private DemoDataRunner runner;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private com.banco.core.dao.CuentaRepository cuentaRepository;

    @Autowired
    private com.banco.core.dao.ClienteRepository clienteRepository;

    @Autowired
    private com.banco.core.dao.EmpleadoRepository empleadoRepository;

    // Los datos demo se confirman de verdad (sin rollback): se limpian para no
    // aparecer en otros tests que comparten la misma base H2.
    @AfterEach
    void limpiar() {
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();
        empleadoRepository.deleteAll();
    }

    @Test
    void cargaClientesCuentasYMovimientosYEsIdempotente() {
        runner.run();
        runner.run();

        assertEquals(3, clienteService.listarTodos().size());
        assertEquals(5, cuentaRepository.count());
        var cuentasMaria = cuentaService.listarPorCliente("12345678-9");
        assertEquals(2, cuentasMaria.size());
        assertTrue(cuentaService.historial(cuentasMaria.get(0).getNumeroCuenta()).size() >= 3);
        assertTrue(clienteService.autenticar("12345678-9", "clave123").isPresent());
    }
}
