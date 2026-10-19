package com.banco.core.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRETO = "secreto-de-prueba-con-mas-de-32-caracteres!!";

    private final JwtService jwtService = new JwtService(SECRETO, 60_000);
    private final CargadorDeIdentidad detailsService = mock(CargadorDeIdentidad.class);
    private final FilterChain cadena = mock(FilterChain.class);
    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwtService, detailsService);

    private MockHttpServletRequest peticion;
    private MockHttpServletResponse respuesta;

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        peticion = new MockHttpServletRequest();
        respuesta = new MockHttpServletResponse();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails usuario(String dui, String rol, boolean activo) {
        return User.withUsername(dui).password("")
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol))
                .disabled(!activo).build();
    }

    @Test
    void tokenValidoDejaLaIdentidadYElRolEnElContexto() throws Exception {
        String token = jwtService.generarToken("12345678-9", "CAJERO");
        when(detailsService.cargar("12345678-9", "CAJERO")).thenReturn(usuario("12345678-9", "CAJERO", true));
        peticion.addHeader("Authorization", "Bearer " + token);

        filtro.doFilter(peticion, respuesta, cadena);

        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacion);
        assertEquals("12345678-9", ((UserDetails) autenticacion.getPrincipal()).getUsername());
        assertTrue(autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CAJERO")));
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    void sinCabeceraNoAutenticaPeroSigueLaCadena() throws Exception {
        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    void tokenInvalidoNoAutenticaPeroSigueLaCadena() throws Exception {
        peticion.addHeader("Authorization", "Bearer esto-no-es-un-jwt");

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    void cabeceraSinPrefijoBearerSeIgnora() throws Exception {
        String token = jwtService.generarToken("12345678-9", "CLIENTE");
        peticion.addHeader("Authorization", token);

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void usuarioDesactivadoNoSeAutenticaAunConTokenVigente() throws Exception {
        String token = jwtService.generarToken("12345678-9", "CLIENTE");
        when(detailsService.cargar("12345678-9", "CLIENTE")).thenReturn(usuario("12345678-9", "CLIENTE", false));
        peticion.addHeader("Authorization", "Bearer " + token);

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    void usuarioInexistenteNoSeAutenticaYNoLanzaExcepcion() throws Exception {
        String token = jwtService.generarToken("99999999-9", "CLIENTE");
        when(detailsService.cargar("99999999-9", "CLIENTE")).thenThrow(new UsernameNotFoundException("no existe"));
        peticion.addHeader("Authorization", "Bearer " + token);

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }
}
