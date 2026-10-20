package com.banco.core.controller;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.model.entity.TipoCuenta;
import com.banco.core.model.entity.TipoTransaccion;
import com.banco.core.security.AutorizacionCuentas;
import com.banco.core.service.CuentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Estos tests cubren solo la delegacion al service; la seguridad (401/403) se
// prueba aparte (SeguridadEndpointsTest), por eso se desactivan los filtros aqui.
@WebMvcTest(CuentaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService cuentaService;

    @MockitoBean
    private AutorizacionCuentas autorizacion;

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

    @Test
    void lasOperacionesDevuelvenElComprobanteGuardado() throws Exception {
        RegistroTransaccion retiro = new RegistroTransaccion(
                "TRX-000009", "Retiro", new BigDecimal("10.00"), LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", null, null, new BigDecimal("90.00"), null,
                "Retiro TRX-000009 | Cuenta AHO-000001 | Monto 10.00 | Estado COMPLETADA");
        when(cuentaService.retirar("AHO-000001", new BigDecimal("10.00"))).thenReturn(retiro);

        mockMvc.perform(post("/api/cuentas/AHO-000001/retiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":10.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comprobante").value(retiro.getComprobante()))
                .andExpect(jsonPath("$.saldoPosterior").value(90.00));
    }

    @Test
    void abrirCuentaDevuelveElComprobanteDeApertura() throws Exception {
        Cuenta cuenta = cuentaPrueba();
        when(autorizacion.titularEfectivo(any(), any())).thenReturn("12345678-9");
        when(cuentaService.abrirCuenta(any(), any(), any(), any())).thenReturn(cuenta);

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                        .content(APERTURA.formatted("AHORROS")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comprobante").value(cuenta.generarComprobanteApertura()));
    }

    @Test
    void consultarUnaCuentaNoLlevaComprobanteDeApertura() throws Exception {
        Cuenta cuenta = cuentaPrueba();
        when(cuentaService.buscarPorNumero(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));

        mockMvc.perform(get("/api/cuentas/" + cuenta.getNumeroCuenta()))
                .andExpect(jsonPath("$.comprobante").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void historialMuestraElSaldoPosteriorDeLaCuentaConsultada() throws Exception {
        RegistroTransaccion transferencia = new RegistroTransaccion(
                "TRX-000002", "Transferencia", new BigDecimal("40.00"), LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", "AHO-000002", null,
                new BigDecimal("60.00"), new BigDecimal("50.00"));
        when(cuentaService.historialFiltrado("AHO-000001", null)).thenReturn(java.util.List.of(transferencia));
        when(cuentaService.historialFiltrado("AHO-000002", null)).thenReturn(java.util.List.of(transferencia));

        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldoPosterior").value(60.00));
        mockMvc.perform(get("/api/cuentas/AHO-000002/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldoPosterior").value(50.00));
    }

    @Test
    void historialDeRegistrosViejosDevuelveSaldoPosteriorNull() throws Exception {
        RegistroTransaccion viejo = new RegistroTransaccion(
                "TRX-000001", "Deposito", new BigDecimal("50.00"), LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", null, null);
        when(cuentaService.historialFiltrado("AHO-000001", null)).thenReturn(java.util.List.of(viejo));

        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldoPosterior").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void historialConTipoFiltraPorEseTipo() throws Exception {
        RegistroTransaccion retiro = new RegistroTransaccion(
                "TRX-000003", "Retiro", new BigDecimal("10.00"), LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", null, null);
        when(cuentaService.historialFiltrado("AHO-000001", TipoTransaccion.RETIRO))
                .thenReturn(java.util.List.of(retiro));

        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones").param("tipo", "retiro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("Retiro"));
    }

    @Test
    void historialConTipoEnBlancoNoFiltra() throws Exception {
        when(cuentaService.historialFiltrado("AHO-000001", null)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones").param("tipo", " "))
                .andExpect(status().isOk());

        verify(cuentaService).historialFiltrado("AHO-000001", null);
    }

    @Test
    void historialConTipoDesconocidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones").param("tipo", "giro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(cuentaService, never()).historialFiltrado(any(), any());
    }

    @Test
    void exportarDevuelveTextoPlanoDescargable() throws Exception {
        RegistroTransaccion deposito = new RegistroTransaccion(
                "TRX-000001", "Deposito", new BigDecimal("50.00"), LocalDateTime.of(2026, 10, 19, 9, 0, 0),
                "COMPLETADA", "AHO-000001", null, null, new BigDecimal("150.00"), null);
        when(cuentaService.historialFiltrado("AHO-000001", null)).thenReturn(java.util.List.of(deposito));

        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"historial-AHO-000001.txt\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "2026-10-19 09:00:00 | TRX-000001 | Deposito | 50.00 | 150.00 | COMPLETADA")));
    }

    @Test
    void exportarRespetaElFiltroPorTipo() throws Exception {
        when(cuentaService.historialFiltrado("AHO-000001", TipoTransaccion.RETIRO)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones/exportar").param("tipo", "RETIRO"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Movimientos: 0")));
    }

    @Test
    void exportarConTipoDesconocidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/cuentas/AHO-000001/transacciones/exportar").param("tipo", "giro"))
                .andExpect(status().isBadRequest());
    }

    private static final String APERTURA = "{\"duiTitular\":\"12345678-9\",\"tipoCuenta\":\"%s\",\"saldoInicial\":100.00}";

    @Test
    void abrirCuentaConvierteElTipoAEnumSinDistinguirMayusculas() throws Exception {
        when(autorizacion.titularEfectivo(any(), any())).thenReturn("12345678-9");
        when(cuentaService.abrirCuenta(any(), any(), any(), any())).thenReturn(cuentaPrueba());

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                        .content(APERTURA.formatted("ahorros")))
                .andExpect(status().isCreated());

        verify(cuentaService).abrirCuenta(eq("12345678-9"), eq(TipoCuenta.AHORROS), any(), any());
    }

    @Test
    void abrirCuentaConTipoDesconocidoDevuelve400ConError() throws Exception {
        when(autorizacion.titularEfectivo(any(), any())).thenReturn("12345678-9");

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                        .content(APERTURA.formatted("VIP")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(cuentaService, never()).abrirCuenta(any(), any(), any(), any());
    }
}
