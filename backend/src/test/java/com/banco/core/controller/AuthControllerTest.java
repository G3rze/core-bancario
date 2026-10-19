package com.banco.core.controller;

import com.banco.core.config.SecurityConfig;
import com.banco.core.exception.CredencialesInvalidasException;
import com.banco.core.model.dto.LoginResponse;
import com.banco.core.model.dto.UsuarioSesionDTO;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.security.CargadorDeIdentidad;
import com.banco.core.security.JwtService;
import com.banco.core.service.AuthService;
import com.banco.core.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Con SecurityConfig real (no se desactivan los filtros) para comprobar de
 * paso que el login es publico: se llama sin Authorization.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "banco.jwt.secret=secreto-de-prueba-con-mas-de-32-caracteres!!",
        "banco.jwt.expiration-ms=60000"
})
class AuthControllerTest {

    private static final String CUERPO = """
            {"dui":"12345678-9","password":"clave123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private CargadorDeIdentidad cargadorDeIdentidad;

    private static final String REGISTRO = """
            {"dui":"12345678-9","nombre":"Ana Lopez","direccion":"San Salvador",
             "telefono":"7777-7777","tipo":"NATURAL","password":"clave123"}
            """;

    @Test
    void loginValidoDevuelveTokenExpiracionYUsuarioSinAuthorization() throws Exception {
        var usuario = new UsuarioSesionDTO("12345678-9", "Ana Lopez", "CLIENTE", null, "CLI-000001");
        when(authService.login("12345678-9", "clave123")).thenReturn(
                new LoginResponse("token-de-prueba", Instant.parse("2026-10-19T22:00:00Z"), usuario));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"))
                .andExpect(jsonPath("$.expiraEn").value("2026-10-19T22:00:00Z"))
                .andExpect(jsonPath("$.usuario.dui").value("12345678-9"))
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.numeroCliente").value("CLI-000001"))
                .andExpect(jsonPath("$.usuario.codigoEmpleado").doesNotExist());
    }

    @Test
    void credencialesInvalidasDevuelven401ConCuerpoError() throws Exception {
        when(authService.login("12345678-9", "clave123"))
                .thenThrow(new CredencialesInvalidasException("DUI o contrasena incorrectos"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("DUI o contrasena incorrectos"));
    }

    @Test
    void registroEsPublicoYDevuelve201ConElClienteSinPassword() throws Exception {
        when(clienteService.registrar(any(), any(), any(), any(), any(), any())).thenReturn(
                new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL));

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dui").value("12345678-9"))
                .andExpect(jsonPath("$.tipo").value("NATURAL"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registroConDuiRepetidoDevuelve400ConCuerpoError() throws Exception {
        when(clienteService.registrar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Ya existe un cliente registrado con DUI 12345678-9"));

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe un cliente registrado con DUI 12345678-9"));
    }
}
