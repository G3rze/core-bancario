package com.banco.core.controller;

import com.banco.core.config.SecurityConfig;
import com.banco.core.exception.CredencialesInvalidasException;
import com.banco.core.model.dto.LoginResponse;
import com.banco.core.model.dto.UsuarioSesionDTO;
import com.banco.core.security.CargadorDeIdentidad;
import com.banco.core.security.JwtService;
import com.banco.core.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

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
    private CargadorDeIdentidad cargadorDeIdentidad;

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
}
