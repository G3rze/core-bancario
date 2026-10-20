package com.banco.core.security;

import com.banco.core.config.CorsConfig;
import com.banco.core.config.SecurityConfig;
import com.banco.core.controller.ClienteController;
import com.banco.core.controller.CuentaController;
import com.banco.core.controller.EmpleadoController;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.TipoCliente;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reglas de acceso de los controllers con tokens JWT reales y la cadena de
 * seguridad completa (SecurityConfig + @PreAuthorize). Los servicios van
 * mockeados: aqui solo se prueba quien puede entrar, no la logica de negocio.
 */
@WebMvcTest({CuentaController.class, ClienteController.class, EmpleadoController.class})
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, AutorizacionCuentasImpl.class})
@TestPropertySource(properties = {
        "banco.jwt.secret=secreto-de-prueba-con-mas-de-32-caracteres!!",
        "banco.jwt.expiration-ms=60000"
})
class SeguridadEndpointsTest {

    private static final String DUI_DUENO = "12345678-9";
    private static final String DUI_OTRO = "98765432-1";

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

    private String numeroCuenta;

    @BeforeEach
    void cuentaDelDueno() {
        Cliente dueno = new Cliente(DUI_DUENO, "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        Cuenta cuenta = new CuentaAhorros(dueno, new BigDecimal("100.00"));
        numeroCuenta = cuenta.getNumeroCuenta();
        when(cuentaService.buscarPorNumero(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(clienteService.listarTodos()).thenReturn(List.of());
        when(cuentaService.listarPorCliente(any())).thenReturn(List.of());
    }

    /** Emite un token real y hace que el cargador lo reconozca con ese rol. */
    private String bearer(String dui, String rol) {
        when(cargadorDeIdentidad.cargar(dui, rol)).thenReturn(
                User.withUsername(dui).password("").authorities(new SimpleGrantedAuthority("ROLE_" + rol)).build());
        return "Bearer " + jwtService.generarToken(dui, rol);
    }

    // ---- cuentas: propiedad ----

    @Test
    void sinTokenUnaCuentaDevuelve401() throws Exception {
        mockMvc.perform(get("/api/cuentas/" + numeroCuenta))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteVeSuPropiaCuenta() throws Exception {
        mockMvc.perform(get("/api/cuentas/" + numeroCuenta).header("Authorization", bearer(DUI_DUENO, "CLIENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoVeLaCuentaDeOtro() throws Exception {
        mockMvc.perform(get("/api/cuentas/" + numeroCuenta).header("Authorization", bearer(DUI_OTRO, "CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeDepositarEnCuentaAjenaYElServicioNiSeInvoca() throws Exception {
        mockMvc.perform(post("/api/cuentas/" + numeroCuenta + "/depositos")
                        .header("Authorization", bearer(DUI_OTRO, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":10.00}"))
                .andExpect(status().isForbidden());

        verify(cuentaService, never()).depositar(any(), any());
    }

    @Test
    void cajeroVeCualquierCuenta() throws Exception {
        mockMvc.perform(get("/api/cuentas/" + numeroCuenta).header("Authorization", bearer(DUI_OTRO, "CAJERO")))
                .andExpect(status().isOk());
    }

    // ---- listados: solo personal ----

    @Test
    void clienteNoListaClientesPeroElCajeroSi() throws Exception {
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(DUI_DUENO, "CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(DUI_OTRO, "CAJERO")))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoListaCuentasDeOtroPorDui() throws Exception {
        mockMvc.perform(get("/api/cuentas").param("duiTitular", DUI_OTRO)
                        .header("Authorization", bearer(DUI_DUENO, "CLIENTE")))
                .andExpect(status().isForbidden());
    }

    // ---- empleados: solo gerente ----

    private static final String NUEVO_CAJERO = """
            {"dui":"11111111-1","nombre":"Marta Diaz","direccion":"San Salvador","telefono":"7777-7777",
             "sucursal":"Centro","rol":"CAJERO","cajaAsignada":"CAJA-01","password":"clave123"}
            """;

    @Test
    void soloUnGerentePuedeRegistrarEmpleados() throws Exception {
        when(empleadoService.registrarCajero(any(), any(), any(), any(), any(), any(), any())).thenReturn(
                new Cajero("11111111-1", "Marta Diaz", "San Salvador", "7777-7777", "Centro", "CAJA-01", "hash"));

        mockMvc.perform(post("/api/empleados").header("Authorization", bearer(DUI_OTRO, "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_CAJERO))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/empleados").header("Authorization", bearer(DUI_OTRO, "GERENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_CAJERO))
                .andExpect(status().isCreated());
    }
}
