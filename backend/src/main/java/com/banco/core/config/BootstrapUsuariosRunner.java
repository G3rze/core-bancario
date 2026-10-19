package com.banco.core.config;

import com.banco.core.service.EmpleadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Crea el primer gerente y el primer administrador al arrancar, con
 * credenciales de .env, para no tener un endpoint abierto "crear el primero".
 *
 * Idempotente: si ya existe alguien de ese rol no hace nada. Si faltan las
 * variables avisa y sigue: la app debe poder arrancar sin ellas. Nunca
 * registra contrasenas en el log. Depende solo de la interfaz EmpleadoService.
 */
@Component
@EnableConfigurationProperties(BootstrapProperties.class)
public class BootstrapUsuariosRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapUsuariosRunner.class);

    // Datos de contacto de relleno: el seeder solo necesita credenciales.
    private static final String DIRECCION = "Casa Matriz";
    private static final String TELEFONO = "2222-0000";
    private static final String SUCURSAL = "Casa Matriz";
    private static final BigDecimal MONTO_MAXIMO_GERENTE = new BigDecimal("10000.00");

    private final EmpleadoService empleadoService;
    private final BootstrapProperties propiedades;

    public BootstrapUsuariosRunner(EmpleadoService empleadoService, BootstrapProperties propiedades) {
        this.empleadoService = empleadoService;
        this.propiedades = propiedades;
    }

    @Override
    public void run(String... args) {
        crearGerenteSiHaceFalta();
        crearAdministradorSiHaceFalta();
    }

    private void crearGerenteSiHaceFalta() {
        if (empleadoService.existeAlgunGerente()) {
            return;
        }
        var datos = propiedades.gerente();
        if (!datos.estaCompleta()) {
            log.warn("No hay gerente y BOOTSTRAP_GERENTE_DUI/PASSWORD no estan configurados: no se crea ninguno");
            return;
        }
        intentar("gerente", datos.dui(), () -> empleadoService.registrarGerente(
                datos.dui(), datos.nombre(), DIRECCION, TELEFONO, SUCURSAL,
                MONTO_MAXIMO_GERENTE, datos.password()));
    }

    private void crearAdministradorSiHaceFalta() {
        if (empleadoService.existeAlgunAdministrador()) {
            return;
        }
        var datos = propiedades.admin();
        if (!datos.estaCompleta()) {
            log.warn("No hay administrador y BOOTSTRAP_ADMIN_DUI/PASSWORD no estan configurados: no se crea ninguno");
            return;
        }
        intentar("administrador", datos.dui(), () -> empleadoService.registrarAdministrador(
                datos.dui(), datos.nombre(), DIRECCION, TELEFONO, SUCURSAL, datos.password()));
    }

    /** Un dato invalido o un DUI repetido no debe impedir que la app arranque. */
    private void intentar(String rol, String dui, Runnable alta) {
        try {
            alta.run();
            log.info("Bootstrap: {} creado con DUI {}", rol, dui);
        } catch (IllegalArgumentException e) {
            log.warn("Bootstrap: no se pudo crear el {} ({}): {}", rol, dui, e.getMessage());
        }
    }
}
