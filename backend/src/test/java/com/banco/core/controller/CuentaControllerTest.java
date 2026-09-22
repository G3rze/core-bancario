package com.banco.core.controller;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.service.CuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService cuentaService;

    private Cuenta cuentaPrueba() {
        Cliente titular = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        return new CuentaAhorros(titular, new BigDecimal("100.00"));
    }

    @Test
    void buscarPorNumeroDevuelve404CuandoNoExiste() throws Exception {
        when(cuentaService.buscarPorNumero("AHO-999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cuentas/AHO-999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorNumeroDevuelveLaCuentaDelService() throws Exception {
        Cuenta cuenta = cuentaPrueba();
        when(cuentaService.buscarPorNumero(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));

        mockMvc.perform(get("/api/cuentas/" + cuenta.getNumeroCuenta()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value(cuenta.getNumeroCuenta()));
    }

    @Test
    void depositarDelegaEnElServiceYDevuelveElComprobante() throws Exception {
        Cuenta cuenta = cuentaPrueba();
        RegistroTransaccion deposito = new RegistroTransaccion(
                "TRX-000001", "Deposito", new BigDecimal("50.00"), LocalDateTime.now(),
                "COMPLETADA", cuenta.getNumeroCuenta(), null, null);
        when(cuentaService.depositar(cuenta.getNumeroCuenta(), new BigDecimal("50.00"))).thenReturn(deposito);

        mockMvc.perform(post("/api/cuentas/" + cuenta.getNumeroCuenta() + "/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroTransaccion").value(deposito.getNumeroTransaccion()))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));
    }
}
