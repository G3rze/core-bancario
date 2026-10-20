package com.banco.core.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres!!";
    private static final String OTRO_SECRETO = "otro-secreto-distinto-con-mas-de-32-caracteres";

    private JwtService servicio() {
        return new JwtService(SECRETO, 60_000);
    }

    @Test
    void tokenValidConservaDuiYRol() {
        JwtService jwt = servicio();

        String token = jwt.generarToken("12345678-9", "CLIENTE");

        assertTrue(jwt.esValido(token));
        assertEquals("12345678-9", jwt.extraerDui(token));
        assertEquals("CLIENTE", jwt.extraerRol(token));
    }

    @Test
    void tokenExpiradoNoEsValido() {
        // Expiracion negativa: el token nace ya vencido.
        JwtService vencido = new JwtService(SECRETO, -1_000);

        String token = vencido.generarToken("12345678-9", "CAJERO");

        assertFalse(vencido.esValido(token));
    }

    @Test
    void tokenFirmadoConOtraClaveNoEsValido() {
        String token = new JwtService(OTRO_SECRETO, 60_000).generarToken("12345678-9", "GERENTE");

        assertFalse(servicio().esValido(token));
    }

    @Test
    void tokenAlteradoNoEsValido() {
        JwtService jwt = servicio();
        String token = jwt.generarToken("12345678-9", "CLIENTE");

        // Cambiar un caracter del medio de la firma (ultimo segmento). No el
        // ultimo: en base64url ese caracter solo aporta 4 bits utiles y
        // cambiarlo a veces no altera los bytes decodificados.
        int posicion = token.length() - 10;
        char original = token.charAt(posicion);
        String alterado = token.substring(0, posicion) + (original == 'A' ? 'B' : 'A')
                + token.substring(posicion + 1);

        assertFalse(jwt.esValido(alterado));
    }

    @Test
    void textoQueNoEsUnTokenNoEsValido() {
        assertFalse(servicio().esValido("esto-no-es-un-jwt"));
        assertFalse(servicio().esValido(""));
    }

    @Test
    void secretoCortoSeRechazaAlConstruir() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 60_000));
    }
}
