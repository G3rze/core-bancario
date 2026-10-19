package com.banco.core.model.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpleadoTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void cajeroAceptaDatosValidosYAsignaCodigoEmpleado() {
        Cajero cajero = new Cajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", "clave123");

        assertEquals(Rol.CAJERO, cajero.getRol());
        assertTrue(cajero.getCodigoEmpleado().startsWith("EMP-"));
    }

    @Test
    void validarPasswordAceptaLaClaveCorrectaYRechazaOtra() {
        Cajero cajero = new Cajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", encoder.encode("clave123"));

        assertTrue(cajero.validarPassword("clave123", encoder));
        assertFalse(cajero.validarPassword("otraClave", encoder));
    }

    @Test
    void rechazaHashVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Cajero("12345678-9", "Marta Diaz",
                "San Salvador", "7777-7777", "Centro", "CAJA-01", " "));
    }

    @Test
    void administradorTieneRolAdminYNivelTotal() {
        Administrador admin = new Administrador("12345678-9", "Root", "San Salvador", "7777-7777",
                "Centro", encoder.encode("clave123"));

        assertEquals(Rol.ADMIN, admin.getRol());
        assertEquals("TOTAL", admin.getNivelAutorizacion());
        assertTrue(admin.getCodigoEmpleado().startsWith("EMP-"));
    }

    @Test
    void gerenteAceptaDatosValidos() {
        Gerente gerente = new Gerente("12345678-9", "Carlos Ruiz", "San Salvador", "7777-7777",
                "Centro", new BigDecimal("5000.00"), "clave123");

        assertEquals(Rol.GERENTE, gerente.getRol());
        assertEquals("SUPERVISOR", gerente.getNivelAutorizacion());
    }

    @Test
    void avanzarContadorEvitaQueUnEmpleadoNuevoRepitaUnCodigoYaUsado() {
        // Ver Cliente/Cuenta/Transaccion avanzarContador: mismo problema
        // (el contador reinicia en cada arranque de la JVM), mismo arreglo.
        Empleado.avanzarContador("EMP-999999");

        Cajero cajero = new Cajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", "clave123");

        long numero = Long.parseLong(cajero.getCodigoEmpleado().substring(4));
        assertTrue(numero > 999999);
    }
}
