package com.banco.core.controller;

import com.banco.core.config.CorsConfig;
import com.banco.core.config.SecurityConfig;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Gerente;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.security.CargadorDeIdentidad;
import com.banco.core.security.JwtService;
import com.banco.core.service.ClienteService;
import com.banco.core.service.CuentaService;
import com.banco.core.service.EmpleadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentanillaController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "banco.jwt.secret=secreto-de-prueba-con-mas-de-32-caracteres!!",
        "banco.jwt.expiration-ms=60000"
})
class VentanillaControllerTest {

    private static final String DUI_CAJERO = "11111111-1";
    private static final String DUI_GERENTE = "22222222-2";
    private static final String CUENTA = "AHO-0001";
    private static final String DESTINO = "AHO-0002";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CuentaService cuentaService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private EmpleadoService empleadoService;

    @MockitoBean
    private CargadorDeIdentidad cargadorDeIdentidad;

    private Cajero cajero;

    @BeforeEach
    void preparar() {
        cajero = new Cajero(DUI_CAJERO, "Marta Diaz", "San Salvador", "7777-7777", "Centro", "CAJA-01", "hash");
        when(empleadoService.buscarPorDui(DUI_CAJERO)).thenReturn(Optional.of(cajero));
        when(empleadoService.buscarPorDui(DUI_GERENTE)).thenReturn(Optional.of(
                new Gerente(DUI_GERENTE, "Luis Perez", "San Salvador", "7777-7777", "Centro",
                        new BigDecimal("5000.00"), "hash")));
    }

    private String bearer(String dui, String rol) {
        when(cargadorDeIdentidad.cargar(dui, rol)).thenReturn(
                User.withUsername(dui).password("").authorities(new SimpleGrantedAuthority("ROLE_" + rol)).build());
        return "Bearer " + jwtService.generarToken(dui, rol);
    }

    private RegistroTransaccion registro(String tipo, BigDecimal monto, String destino) {
        return new RegistroTransaccion("T-1", tipo, monto, LocalDateTime.now(), "EJECUTADA",
                CUENTA, destino, cajero.getCodigoEmpleado());
    }

    @Test
    void depositoUsaAlCajeroDelTokenEIgnoraElCodigoDelBody() throws Exception {
        when(cuentaService.depositarEnVentanilla(eq(CUENTA), any(), eq(cajero)))
                .thenReturn(registro("Deposito", new BigDecimal("50.00"), null));

        mockMvc.perform(post("/api/ventanilla/cuentas/" + CUENTA + "/depositos")
                        .header("Authorization", bearer(DUI_CAJERO, "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00,\"codigoEmpleadoCajero\":\"OTRO-99\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoEmpleadoCajero").value(cajero.getCodigoEmpleado()));

        verify(cuentaService).depositarEnVentanilla(CUENTA, new BigDecimal("50.00"), cajero);
    }

    @Test
    void retiroUsaAlCajeroDelToken() throws Exception {
        when(cuentaService.retirarEnVentanilla(eq(CUENTA), any(), eq(cajero)))
                .thenReturn(registro("Retiro", new BigDecimal("30.00"), null));

        mockMvc.perform(post("/api/ventanilla/cuentas/" + CUENTA + "/retiros")
                        .header("Authorization", bearer(DUI_CAJERO, "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":30.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoEmpleadoCajero").value(cajero.getCodigoEmpleado()));

        verify(cuentaService).retirarEnVentanilla(CUENTA, new BigDecimal("30.00"), cajero);
    }

    @Test
    void transferenciaUsaAlCajeroDelToken() throws Exception {
        when(cuentaService.transferirEnVentanilla(eq(CUENTA), eq(DESTINO), any(), eq(cajero)))
                .thenReturn(registro("Transferencia", new BigDecimal("20.00"), DESTINO));

        mockMvc.perform(post("/api/ventanilla/cuentas/" + CUENTA + "/transferencias")
                        .header("Authorization", bearer(DUI_CAJERO, "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroCuentaDestino\":\"" + DESTINO + "\",\"monto\":20.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Transferencia"));

        verify(cuentaService).transferirEnVentanilla(CUENTA, DESTINO, new BigDecimal("20.00"), cajero);
    }

    @Test
    void gerenteNoMueveDineroEnVentanillaPeroSiConsultaClientes() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + CUENTA + "/depositos")
                        .header("Authorization", bearer(DUI_GERENTE, "GERENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isForbidden());

        verify(cuentaService, never()).depositarEnVentanilla(any(), any(), any());

        Cliente cliente = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        when(clienteService.buscarPorDuiONumeroCliente("12345678-9")).thenReturn(Optional.of(cliente));
        when(cuentaService.listarPorCliente("12345678-9")).thenReturn(List.of());

        mockMvc.perform(get("/api/ventanilla/clientes/12345678-9")
                        .header("Authorization", bearer(DUI_GERENTE, "GERENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.dui").value("12345678-9"));
    }

    @Test
    void clienteNoEntraAVentanilla() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + CUENTA + "/depositos")
                        .header("Authorization", bearer("12345678-9", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isForbidden());

        verify(cuentaService, never()).depositarEnVentanilla(any(), any(), any());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + CUENTA + "/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isUnauthorized());
    }
}
