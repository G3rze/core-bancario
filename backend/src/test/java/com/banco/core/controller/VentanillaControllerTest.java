package com.banco.core.controller;

import com.banco.core.config.CorsConfig;
import com.banco.core.config.SecurityConfig;
import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.CuentaRepository;
import com.banco.core.dao.TransaccionDAO;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.Gerente;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.security.CargadorDeIdentidad;
import com.banco.core.security.JwtService;
import com.banco.core.service.EmpleadoService;
import com.banco.core.service.impl.ClienteServiceImpl;
import com.banco.core.service.impl.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El controller depende hoy de CuentaServiceImpl/ClienteServiceImpl (clases
 * concretas que Mockito no puede mockear en esta JVM), asi que se usan las
 * implementaciones reales sobre repositorios mockeados. Cuando B1 las
 * cambie por interfaces, esto se simplifica a @MockitoBean.
 */
@WebMvcTest(VentanillaController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class,
        VentanillaControllerTest.ServiciosReales.class})
@TestPropertySource(properties = {
        "banco.jwt.secret=secreto-de-prueba-con-mas-de-32-caracteres!!",
        "banco.jwt.expiration-ms=60000"
})
class VentanillaControllerTest {

    private static final String DUI_CAJERO = "11111111-1";
    private static final String DUI_GERENTE = "22222222-2";

    @TestConfiguration
    static class ServiciosReales {
        @Bean
        CuentaRepository cuentaRepository() {
            return mock(CuentaRepository.class);
        }

        @Bean
        ClienteRepository clienteRepository() {
            return mock(ClienteRepository.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        @Bean
        ClienteServiceImpl clienteServiceImpl(ClienteRepository repo, PasswordEncoder encoder) {
            return new ClienteServiceImpl(repo, encoder);
        }

        @Bean
        CuentaServiceImpl cuentaServiceImpl(CuentaRepository cuentas, ClienteRepository clientes) throws IOException {
            Path archivo = Files.createTempFile("transacciones", ".dat");
            Files.delete(archivo);
            archivo.toFile().deleteOnExit();
            return new CuentaServiceImpl(cuentas, clientes, new TransaccionDAO(archivo));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @MockitoBean
    private EmpleadoService empleadoService;

    @MockitoBean
    private CargadorDeIdentidad cargadorDeIdentidad;

    private Cuenta cuenta;
    private Cajero cajero;

    @BeforeEach
    void preparar() {
        Cliente titular = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        cuenta = new CuentaAhorros(titular, new BigDecimal("100.00"));
        when(cuentaRepository.findByNumeroCuenta(cuenta.getNumeroCuenta())).thenReturn(Optional.of(cuenta));

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

    @Test
    void depositoUsaAlCajeroDelTokenEIgnoraElCodigoDelBody() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + cuenta.getNumeroCuenta() + "/depositos")
                        .header("Authorization", bearer(DUI_CAJERO, "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00,\"codigoEmpleadoCajero\":\"OTRO-99\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoEmpleadoCajero").value(cajero.getCodigoEmpleado()));

        assertEquals(new BigDecimal("150.00"), cuenta.getSaldo());
    }

    @Test
    void retiroUsaAlCajeroDelToken() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + cuenta.getNumeroCuenta() + "/retiros")
                        .header("Authorization", bearer(DUI_CAJERO, "CAJERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":30.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoEmpleadoCajero").value(cajero.getCodigoEmpleado()));

        assertEquals(new BigDecimal("70.00"), cuenta.getSaldo());
    }

    @Test
    void gerenteNoMueveDineroEnVentanilla() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + cuenta.getNumeroCuenta() + "/depositos")
                        .header("Authorization", bearer(DUI_GERENTE, "GERENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isForbidden());

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldo());
    }

    @Test
    void clienteNoEntraAVentanilla() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + cuenta.getNumeroCuenta() + "/depositos")
                        .header("Authorization", bearer("12345678-9", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/ventanilla/cuentas/" + cuenta.getNumeroCuenta() + "/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50.00}"))
                .andExpect(status().isUnauthorized());
    }
}
