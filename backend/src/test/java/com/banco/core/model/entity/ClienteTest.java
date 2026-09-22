package com.banco.core.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteTest {

    @Test
    void rechazaDuiConFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("12345678", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL));
    }

    @Test
    void rechazaTelefonoConFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("12345678-9", "Ana Lopez", "San Salvador", "77777777", TipoCliente.NATURAL));
    }

    @Test
    void aceptaDatosValidosYAsignaNumeroDeCliente() {
        Cliente cliente = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

        assertEquals("CLIENTE", cliente.getRol());
        assertTrue(cliente.getNumeroCliente().startsWith("CLI-"));
    }

    @Test
    void avanzarContadorEvitaQueUnClienteNuevoRepitaUnNumeroYaUsado() {
        // Simula lo que ClienteServiceImpl.cargarDesdeDisco() hace al
        // reiniciar la JVM con clientes ya persistidos: sin este avance, el
        // contador estatico (que arranca en 1 en cada arranque) podria
        // repetir un numero ya usado por un cliente cargado del .dat.
        Cliente.avanzarContador("CLI-999999");

        Cliente cliente = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

        long numero = Long.parseLong(cliente.getNumeroCliente().substring(4));
        assertTrue(numero > 999999);
    }
}
