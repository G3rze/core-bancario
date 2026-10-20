package com.banco.core.service;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.EmpleadoRepository;
import com.banco.core.exception.UsuarioNoEncontradoException;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.model.entity.Usuario;
import com.banco.core.service.impl.UsuarioAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class UsuarioAdminServiceImplTest {

    private static final String DUI = "12345678-9";
    private static final String ADMIN = "00000000-0";

    private ClienteRepository clienteRepository;
    private EmpleadoRepository empleadoRepository;
    private UsuarioAdminServiceImpl servicio;

    @BeforeEach
    void preparar() {
        clienteRepository = Mockito.mock(ClienteRepository.class);
        empleadoRepository = Mockito.mock(EmpleadoRepository.class);
        servicio = new UsuarioAdminServiceImpl(clienteRepository, empleadoRepository);
        when(clienteRepository.findByDui(Mockito.any())).thenReturn(Optional.empty());
        when(empleadoRepository.findByDui(Mockito.any())).thenReturn(Optional.empty());
    }

    private Cliente cliente() {
        return new Cliente(DUI, "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    }

    private Cajero cajero() {
        return new Cajero(DUI, "Ana Lopez", "San Salvador", "7777-7777", "Central", "Caja 1", "hash");
    }

    @Test
    void desactivaAUnCliente() {
        Cliente c = cliente();
        when(clienteRepository.findByDui(DUI)).thenReturn(Optional.of(c));

        List<Usuario> r = servicio.cambiarEstado(DUI, false, ADMIN);

        assertEquals(1, r.size());
        assertFalse(c.isActivo());
    }

    @Test
    void reactivaAUnEmpleado() {
        Cajero e = cajero();
        e.desactivar();
        when(empleadoRepository.findByDui(DUI)).thenReturn(Optional.of(e));

        servicio.cambiarEstado(DUI, true, ADMIN);

        assertTrue(e.isActivo());
    }

    @Test
    void afectaAmbosRegistrosSiLaPersonaEsClienteYEmpleado() {
        Cliente c = cliente();
        Cajero e = cajero();
        when(clienteRepository.findByDui(DUI)).thenReturn(Optional.of(c));
        when(empleadoRepository.findByDui(DUI)).thenReturn(Optional.of(e));

        List<Usuario> r = servicio.cambiarEstado(DUI, false, ADMIN);

        assertEquals(2, r.size());
        assertFalse(c.isActivo());
        assertFalse(e.isActivo());
    }

    @Test
    void duiInexistenteLanzaNoEncontrado() {
        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.cambiarEstado(DUI, false, ADMIN));
    }

    @Test
    void unAdminNoPuedeDesactivarseASiMismo() {
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarEstado(ADMIN, false, ADMIN));
    }

    @Test
    void unAdminSiPuedeReactivarseASiMismo() {
        Cajero e = cajero();
        when(empleadoRepository.findByDui(DUI)).thenReturn(Optional.of(e));

        servicio.cambiarEstado(DUI, true, DUI);

        assertTrue(e.isActivo());
    }
}
