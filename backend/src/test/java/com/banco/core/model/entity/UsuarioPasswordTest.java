package com.banco.core.model.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La contrasena vive en Usuario: Cliente y Empleado la comparten por herencia. */
class UsuarioPasswordTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private Cliente cliente() {
        return new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    }

    @Test
    void clienteNuevoNoTienePasswordYNoValidaNingunIntento() {
        Cliente cliente = cliente();

        assertFalse(cliente.tienePassword());
        assertFalse(cliente.validarPassword("clave123", encoder));
    }

    @Test
    void clienteConHashValidaLaClaveCorrectaYRechazaOtra() {
        Cliente cliente = cliente();

        cliente.setPasswordHash(encoder.encode("clave123"));

        assertTrue(cliente.tienePassword());
        assertTrue(cliente.validarPassword("clave123", encoder));
        assertFalse(cliente.validarPassword("otraClave", encoder));
        assertFalse(cliente.validarPassword(null, encoder));
    }

    @Test
    void rechazaHashVacioONulo() {
        Cliente cliente = cliente();

        assertThrows(IllegalArgumentException.class, () -> cliente.setPasswordHash(" "));
        assertThrows(IllegalArgumentException.class, () -> cliente.setPasswordHash(null));
    }

    @Test
    void rolEsUnEnumPorSubtipo() {
        Cajero cajero = new Cajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", "clave123");

        assertTrue(cliente().getRol() == Rol.CLIENTE && cajero.getRol() == Rol.CAJERO);
    }
}
