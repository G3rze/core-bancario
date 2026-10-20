package com.banco.core.service.impl;

import com.banco.core.dao.EmpleadoRepository;
import com.banco.core.model.entity.Empleado;
import com.banco.core.model.entity.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unitario con Mockito: no toca la base, solo la logica de contrasenas. */
class EmpleadoServiceImplTest {

    private final EmpleadoRepository repositorio = mock(EmpleadoRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final EmpleadoServiceImpl servicio = new EmpleadoServiceImpl(repositorio, encoder);

    private Empleado registrarCajero(String password) {
        return servicio.registrarCajero("12345678-9", "Marta Diaz", "San Salvador", "7777-7777",
                "Centro", "CAJA-01", password);
    }

    @Test
    void registrarCajeroGuardaElHashBcryptYNuncaElTextoPlano() {
        when(repositorio.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado cajero = registrarCajero("clave123");

        assertTrue(cajero.tienePassword());
        assertTrue(cajero.validarPassword("clave123", encoder));
        assertFalse(cajero.validarPassword("otraClave", encoder));
    }

    @Test
    void rechazaPasswordCortaAntesDeTocarElRepositorio() {
        assertThrows(IllegalArgumentException.class, () -> registrarCajero("123"));

        verify(repositorio, never()).save(any());
    }

    @Test
    void registrarAdministradorCreaUnEmpleadoConRolAdmin() {
        when(repositorio.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado admin = servicio.registrarAdministrador("12345678-9", "Root", "San Salvador",
                "7777-7777", "Centro", "clave123");

        assertEquals(Rol.ADMIN, admin.getRol());
        assertTrue(admin.validarPassword("clave123", encoder));
    }

    @Test
    void registrarGerenteCodificaLaPassword() {
        when(repositorio.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado gerente = servicio.registrarGerente("12345678-9", "Carlos Ruiz", "San Salvador",
                "7777-7777", "Centro", new BigDecimal("5000.00"), "clave123");

        assertTrue(gerente.validarPassword("clave123", encoder));
    }

    @Test
    void autenticarAceptaLaClaveCorrectaYRechazaLaIncorrecta() {
        when(repositorio.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
        Empleado cajero = registrarCajero("clave123");
        when(repositorio.findByDui("12345678-9")).thenReturn(Optional.of(cajero));

        assertTrue(servicio.autenticar("12345678-9", "clave123").isPresent());
        assertTrue(servicio.autenticar("12345678-9", "mala").isEmpty());
        assertTrue(servicio.autenticar("00000000-0", "clave123").isEmpty());
    }

    @Test
    void existeAlgunAdministradorSeBasaEnElConteoDelRepositorio() {
        when(repositorio.contarAdministradores()).thenReturn(0L, 1L);

        assertFalse(servicio.existeAlgunAdministrador());
        assertTrue(servicio.existeAlgunAdministrador());
    }

    @Test
    void buscarPorDuiDelegaEnElRepositorio() {
        Empleado cajero = registrarCajeroGuardado();
        when(repositorio.findByDui("12345678-9")).thenReturn(Optional.of(cajero));

        assertEquals(Optional.of(cajero), servicio.buscarPorDui("12345678-9"));
        assertTrue(servicio.buscarPorDui("00000000-0").isEmpty());
    }

    private Empleado registrarCajeroGuardado() {
        when(repositorio.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
        return registrarCajero("clave123");
    }
}
