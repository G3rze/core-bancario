package com.banco.core.config;

import com.banco.core.controller.ClienteController;
import com.banco.core.security.CargadorDeIdentidad;
import com.banco.core.security.JwtService;
import com.banco.core.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprueba solo el cableado de SecurityConfig (401 sin identidad, token
 * valido pasa, preflight CORS libre). Las reglas por rol y propiedad se
 * prueban aparte en SeguridadEndpointsTest (P7).
 */
@WebMvcTest(ClienteController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "banco.jwt.secret=secreto-de-prueba-con-mas-de-32-caracteres!!",
        "banco.jwt.expiration-ms=60000"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private CargadorDeIdentidad cargadorDeIdentidad;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer basura"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenValidoPermitePasarALaRuta() throws Exception {
        String token = jwtService.generarToken("12345678-9", "CAJERO");
        when(cargadorDeIdentidad.cargar("12345678-9", "CAJERO")).thenReturn(
                User.withUsername("12345678-9").password("")
                        .authorities(new SimpleGrantedAuthority("ROLE_CAJERO")).build());
        when(clienteService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void preflightCorsNoRequiereToken() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
