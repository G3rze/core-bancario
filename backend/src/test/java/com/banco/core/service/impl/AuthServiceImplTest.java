package com.banco.core.service.impl;

import com.banco.core.exception.CredencialesInvalidasException;
import com.banco.core.model.dto.LoginResponse;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.security.JwtService;
import com.banco.core.service.ClienteService;
import com.banco.core.service.EmpleadoService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private final EmpleadoService empleados = mock(EmpleadoService.class);
    private final ClienteService clientes = mock(ClienteService.class);
    private final JwtService jwt = new JwtService("secreto-de-prueba-con-mas-de-32-caracteres!!", 60_000);
    private final AuthServiceImpl servicio = new AuthServiceImpl(empleados, clientes, jwt);

    private Cliente cliente() {
        return new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    }

    private Cajero cajero() {
        return new Cajero("98765432-1", "Marta Diaz", "San Salvador", "7777-7777", "Centro", "CAJA-01", "hash");
    }

    @Test
    void loginDeClienteDevuelveTokenConRolYNumeroCliente() {
        Cliente cliente = cliente();
        when(empleados.autenticar("12345678-9", "clave123")).thenReturn(Optional.empty());
        when(clientes.autenticar("12345678-9", "clave123")).thenReturn(Optional.of(cliente));

        LoginResponse respuesta = servicio.login("12345678-9", "clave123");

        assertTrue(jwt.esValido(respuesta.token()));
        assertEquals("12345678-9", jwt.extraerDui(respuesta.token()));
        assertEquals("CLIENTE", jwt.extraerRol(respuesta.token()));
        assertEquals("CLIENTE", respuesta.usuario().rol());
        assertEquals(cliente.getNumeroCliente(), respuesta.usuario().numeroCliente());
        assertNull(respuesta.usuario().codigoEmpleado());
        assertTrue(respuesta.expiraEn().isAfter(Instant.now()));
    }

    @Test
    void loginDeEmpleadoDevuelveRolYCodigoEmpleado() {
        Cajero cajero = cajero();
        when(empleados.autenticar("98765432-1", "clave123")).thenReturn(Optional.of(cajero));

        LoginResponse respuesta = servicio.login("98765432-1", "clave123");

        assertEquals("CAJERO", jwt.extraerRol(respuesta.token()));
        assertEquals(cajero.getCodigoEmpleado(), respuesta.usuario().codigoEmpleado());
        assertNull(respuesta.usuario().numeroCliente());
    }

    @Test
    void siElDuiEsDeEmpleadoYClienteGanaElQueCoincideConLaClave() {
        // Misma persona en ambas tablas, clave de cliente distinta a la de empleado.
        when(empleados.autenticar("12345678-9", "claveCliente")).thenReturn(Optional.empty());
        when(clientes.autenticar("12345678-9", "claveCliente")).thenReturn(Optional.of(cliente()));

        assertEquals("CLIENTE", servicio.login("12345678-9", "claveCliente").usuario().rol());
    }

    @Test
    void credencialesIncorrectasLanzanCredencialesInvalidas() {
        when(empleados.autenticar("12345678-9", "mala")).thenReturn(Optional.empty());
        when(clientes.autenticar("12345678-9", "mala")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> servicio.login("12345678-9", "mala"));
    }

    @Test
    void duiOPasswordNulosSeRechazanSinConsultarServicios() {
        assertThrows(CredencialesInvalidasException.class, () -> servicio.login(null, "x"));
        assertThrows(CredencialesInvalidasException.class, () -> servicio.login("12345678-9", null));
    }

    @Test
    void usuarioDesactivadoNoPuedeIniciarSesion() {
        Cliente cliente = cliente();
        cliente.desactivar();
        when(empleados.autenticar("12345678-9", "clave123")).thenReturn(Optional.empty());
        when(clientes.autenticar("12345678-9", "clave123")).thenReturn(Optional.of(cliente));

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login("12345678-9", "clave123"));
        assertNotNull(ex.getMessage());
    }
}
