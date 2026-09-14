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
}
