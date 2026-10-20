package com.banco.core.security;

import com.banco.core.config.CorsConfig;
import com.banco.core.config.SecurityConfig;
import com.banco.core.controller.AdminUsuarioController;
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
import com.banco.core.service.UsuarioAdminService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reglas de acceso de los controllers con tokens JWT reales y la cadena de
 * seguridad completa (SecurityConfig + @PreAuthorize). Los servicios van
 * mockeados: aqui solo se prueba quien puede entrar, no la logica de negocio.
 */
@WebMvcTest({CuentaController.class, ClienteController.class, EmpleadoController.class,
        AdminUsuarioController.class})
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
    private UsuarioAdminService usuarioAdminService;

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

    // ---- apertura y /mias ----

    private static final String APERTURA_A_NOMBRE_DE_OTRO = """
            {"duiTitular":"98765432-1","tipoCuenta":"AHORROS","saldoInicial":100.00}
            """;

    private Cuenta cuentaRecienAbierta() {
        Cliente titular = new Cliente(DUI_DUENO, "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        return new CuentaAhorros(titular, new BigDecimal("100.00"));
    }

    @Test
    void clienteAbreCuentaSiempreASuNombreIgnorandoElDuiDelBody() throws Exception {
        when(cuentaService.abrirCuenta(any(), any(), any(), any())).thenReturn(cuentaRecienAbierta());

        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer(DUI_DUENO, "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(APERTURA_A_NOMBRE_DE_OTRO))
                .andExpect(status().isCreated());

        verify(cuentaService).abrirCuenta(eq(DUI_DUENO), eq("AHORROS"), any(), any());
        verify(cuentaService, never()).abrirCuenta(eq(DUI_OTRO), any(), any(), any());
    }

    @Test
    void cajeroAbreCuentaAlDuiIndicado() throws Exception {
        when(cuentaService.abrirCuenta(any(), any(), any(), any())).thenReturn(cuentaRecienAbierta());

        mockMvc.perform(post("/api/cuentas").header("Authorization", bearer("55555555-5", "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON).content(APERTURA_A_NOMBRE_DE_OTRO))
                .andExpect(status().isCreated());

        verify(cuentaService).abrirCuenta(eq(DUI_OTRO), eq("AHORROS"), any(), any());
    }

    @Test
    void misCuentasDevuelveSoloLasDelClienteAutenticado() throws Exception {
        Cuenta cuenta = cuentaRecienAbierta();
        when(cuentaService.listarPorCliente(DUI_DUENO)).thenReturn(List.of(cuenta));

        mockMvc.perform(get("/api/cuentas/mias").header("Authorization", bearer(DUI_DUENO, "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value(cuenta.getNumeroCuenta()));

        verify(cuentaService).listarPorCliente(DUI_DUENO);
        verify(cuentaService, never()).listarPorCliente(DUI_OTRO);
    }

    @Test
    void misCuentasEsSoloParaClientesYRequiereToken() throws Exception {
        mockMvc.perform(get("/api/cuentas/mias").header("Authorization", bearer(DUI_OTRO, "CAJERO")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/cuentas/mias"))
                .andExpect(status().isUnauthorized());
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

    // ---- admin: activar/desactivar usuarios ----

    private static final String CUERPO_DESACTIVAR = "{\"activo\":false}";

    @Test
    void adminDesactivaUnUsuario() throws Exception {
        Cliente objetivo = new Cliente(DUI_DUENO, "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        objetivo.desactivar();
        when(usuarioAdminService.cambiarEstado(DUI_DUENO, false, DUI_OTRO)).thenReturn(List.of(objetivo));

        mockMvc.perform(patch("/api/admin/usuarios/" + DUI_DUENO + "/estado")
                        .header("Authorization", bearer(DUI_OTRO, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_DESACTIVAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dui").value(DUI_DUENO))
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.roles[0]").value("CLIENTE"));
    }

    @Test
    void gerenteNoPuedeCambiarElEstadoDeUsuarios() throws Exception {
        mockMvc.perform(patch("/api/admin/usuarios/" + DUI_DUENO + "/estado")
                        .header("Authorization", bearer(DUI_OTRO, "GERENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_DESACTIVAR))
                .andExpect(status().isForbidden());

        verify(usuarioAdminService, never()).cambiarEstado(any(), org.mockito.ArgumentMatchers.anyBoolean(), any());
    }

    @Test
    void sinTokenElCambioDeEstadoDevuelve401() throws Exception {
        mockMvc.perform(patch("/api/admin/usuarios/" + DUI_DUENO + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_DESACTIVAR))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cuerpoSinActivoDevuelve400() throws Exception {
        mockMvc.perform(patch("/api/admin/usuarios/" + DUI_DUENO + "/estado")
                        .header("Authorization", bearer(DUI_OTRO, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
