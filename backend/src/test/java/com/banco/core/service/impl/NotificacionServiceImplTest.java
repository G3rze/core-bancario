package com.banco.core.service.impl;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.Deposito;
import com.banco.core.model.entity.Notificacion;
import com.banco.core.model.entity.Retiro;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.model.entity.Transaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificacionServiceImplTest {

    private final NotificacionServiceImpl servicio = new NotificacionServiceImpl();
    private final CuentaAhorros cuenta = new CuentaAhorros(
            new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL),
            new BigDecimal("100.00"));

    private Transaccion deposito() {
        Transaccion t = new Deposito(cuenta, new BigDecimal("50.00"));
        t.ejecutar();
        return t;
    }

    @Test
    void sinNotificacionesDevuelveListaVacia() {
        assertTrue(servicio.obtenerPendientes().isEmpty());
    }

    @Test
    void entregaLasNotificacionesEnOrdenFifo() {
        Transaccion primera = deposito();
        Transaccion segunda = new Retiro(cuenta, new BigDecimal("10.00"));
        segunda.ejecutar();
        servicio.registrar(primera);
        servicio.registrar(segunda);

        List<Notificacion> pendientes = servicio.obtenerPendientes();

        assertEquals(2, pendientes.size());
        assertTrue(pendientes.get(0).mensaje().contains(primera.getNumeroTransaccion()));
        assertTrue(pendientes.get(1).mensaje().contains(segunda.getNumeroTransaccion()));
    }

    @Test
    void cadaNotificacionSeEntregaUnaSolaVez() {
        servicio.registrar(deposito());

        assertEquals(1, servicio.obtenerPendientes().size());
        assertTrue(servicio.obtenerPendientes().isEmpty());
    }
}
