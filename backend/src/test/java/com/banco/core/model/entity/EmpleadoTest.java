package com.banco.core.model.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpleadoTest {

    @Test
    void cajeroAceptaDatosValidosYAsignaCodigoEmpleado() {
        Cajero cajero = new Cajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", "clave123");

        assertEquals("CAJERO", cajero.getRol());
        assertTrue(cajero.getCodigoEmpleado().startsWith("EMP-"));
    }

    @Test
    void validarPasswordAceptaLaClaveCorrectaYRechazaOtra() {
        Cajero cajero = new Cajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", "clave123");

        assertTrue(cajero.validarPassword("clave123"));
        assertFalse(cajero.validarPassword("otraClave"));
    }

    @Test
    void rechazaPasswordDemasiadoCorta() {
        assertThrows(IllegalArgumentException.class, () -> new Cajero("12345678-9", "Marta Diaz",
                "San Salvador", "7777-7777", "Centro", "CAJA-01", "123"));
    }

    @Test
    void gerenteAceptaDatosValidos() {
        Gerente gerente = new Gerente("12345678-9", "Carlos Ruiz", "San Salvador", "7777-7777",
                "Centro", new BigDecimal("5000.00"), "clave123");

        assertEquals("GERENTE", gerente.getRol());
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
