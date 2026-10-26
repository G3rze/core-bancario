package com.banco.core.config;

import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.model.entity.TipoCuenta;
import com.banco.core.service.ClienteService;
import com.banco.core.service.CuentaService;
import com.banco.core.service.EmpleadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Datos de demostracion (cajero, clientes, cuentas y movimientos) para poder
 * recorrer la aplicacion recien instalada. Solo corre con DEMO_DATA=true.
 * <p>
 * Pasa por los servicios (no por los repositorios) para respetar las mismas
 * validaciones, bloqueos y comprobantes que una operacion real. Idempotente:
 * si el cajero demo ya existe, no hace nada.
 */
@Component
@ConditionalOnProperty(name = "banco.demo.enabled", havingValue = "true")
public class DemoDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataRunner.class);

    static final String DUI_CAJERO = "33333333-3";
    static final String PASSWORD_CAJERO = "cajero123";
    static final String PASSWORD_CLIENTES = "clave123";

    private final EmpleadoService empleadoService;
    private final ClienteService clienteService;
    private final CuentaService cuentaService;

    public DemoDataRunner(EmpleadoService empleadoService, ClienteService clienteService,
                          CuentaService cuentaService) {
        this.empleadoService = empleadoService;
        this.clienteService = clienteService;
        this.cuentaService = cuentaService;
    }

    @Override
    public void run(String... args) {
        if (empleadoService.buscarPorDui(DUI_CAJERO).isPresent()) {
            return;
        }
        try {
            cargar();
            log.info("Datos de demostracion cargados (cajero {}, 3 clientes, 5 cuentas)", DUI_CAJERO);
        } catch (RuntimeException e) {
            // Los datos demo son opcionales: un fallo no debe impedir que la app arranque.
            log.warn("No se pudieron cargar los datos de demostracion", e);
        }
    }

    private void cargar() {
        Cajero cajero = (Cajero) empleadoService.registrarCajero(DUI_CAJERO, "Marta Diaz", "Santa Tecla",
                "7000-1111", "Sucursal Centro", "CAJA-01", PASSWORD_CAJERO);

        clienteService.registrar("12345678-9", "Maria Lopez", "Col. Escalon, San Salvador", "7777-7777",
                TipoCliente.NATURAL, PASSWORD_CLIENTES);
        clienteService.registrar("87654321-0", "Juan Perez", "Santa Ana", "7888-8888",
                TipoCliente.NATURAL, PASSWORD_CLIENTES);
        clienteService.registrar("11111111-1", "Comercial El Sol S.A.", "San Miguel", "2600-0000",
                TipoCliente.JURIDICA, PASSWORD_CLIENTES);

        Cuenta ahorrosMaria = cuentaService.abrirCuenta("12345678-9", TipoCuenta.AHORROS, new BigDecimal("500.00"), null);
        cuentaService.abrirCuenta("12345678-9", TipoCuenta.CORRIENTE, new BigDecimal("250.00"), null);
        Cuenta ahorrosJuan = cuentaService.abrirCuenta("87654321-0", TipoCuenta.AHORROS, new BigDecimal("300.00"), null);
        cuentaService.abrirCuenta("87654321-0", TipoCuenta.PLAZO_FIJO, new BigDecimal("1000.00"), 12);
        Cuenta corrienteEmpresa = cuentaService.abrirCuenta("11111111-1", TipoCuenta.CORRIENTE, new BigDecimal("5000.00"), null);

        cuentaService.depositar(ahorrosMaria.getNumeroCuenta(), new BigDecimal("120.00"));
        cuentaService.retirar(ahorrosMaria.getNumeroCuenta(), new BigDecimal("50.00"));
        cuentaService.transferir(ahorrosMaria.getNumeroCuenta(), ahorrosJuan.getNumeroCuenta(), new BigDecimal("75.00"));
        cuentaService.depositarEnVentanilla(ahorrosJuan.getNumeroCuenta(), new BigDecimal("40.00"), cajero);
        cuentaService.retirarEnVentanilla(corrienteEmpresa.getNumeroCuenta(), new BigDecimal("300.00"), cajero);
        cuentaService.transferir(corrienteEmpresa.getNumeroCuenta(), ahorrosMaria.getNumeroCuenta(), new BigDecimal("200.00"));
    }
}
