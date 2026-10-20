package com.banco.core.config;

import com.banco.core.config.BootstrapProperties.Credencial;
import com.banco.core.service.EmpleadoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BootstrapUsuariosRunnerTest {

    private final EmpleadoService empleados = mock(EmpleadoService.class);

    private static final Credencial GERENTE = new Credencial("00000001-1", "Gerente General", "clave123");
    private static final Credencial ADMIN = new Credencial("00000002-2", "Administrador", "clave456");
    private static final Credencial VACIA = new Credencial("", "x", "");

    private BootstrapUsuariosRunner runner(Credencial gerente, Credencial admin) {
        return new BootstrapUsuariosRunner(empleados, new BootstrapProperties(gerente, admin));
    }

    @Test
    void creaGerenteYAdministradorCuandoNoExistenYHayCredenciales() {
        runner(GERENTE, ADMIN).run();

        verify(empleados).registrarGerente(anyString(), anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString());
        verify(empleados).registrarAdministrador("00000002-2", "Administrador", "Casa Matriz",
                "2222-0000", "Casa Matriz", "clave456");
    }

    @Test
    void esIdempotenteSiYaExistenAmbos() {
        when(empleados.existeAlgunGerente()).thenReturn(true);
        when(empleados.existeAlgunAdministrador()).thenReturn(true);

        runner(GERENTE, ADMIN).run();

        verify(empleados, never()).registrarGerente(any(), any(), any(), any(), any(), any(), any());
        verify(empleados, never()).registrarAdministrador(any(), any(), any(), any(), any(), any());
    }

    @Test
    void sinCredencialesNoCreaNadaYNoFalla() {
        assertDoesNotThrow(() -> runner(VACIA, VACIA).run());

        verify(empleados, never()).registrarGerente(any(), any(), any(), any(), any(), any(), any());
        verify(empleados, never()).registrarAdministrador(any(), any(), any(), any(), any(), any());
    }

    @Test
    void unaCredencialInvalidaNoImpideElArranqueNiElOtroAlta() {
        when(empleados.registrarGerente(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres"));

        assertDoesNotThrow(() -> runner(GERENTE, ADMIN).run());

        verify(empleados).registrarAdministrador(any(), any(), any(), any(), any(), any());
    }
}
