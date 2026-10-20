package com.banco.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee "Authorization: Bearer <token>" y, si el token es valido y el usuario
 * sigue existiendo y activo, deja la identidad en el SecurityContext.
 *
 * No responde 401 por si mismo: ante un token ausente o invalido solo deja
 * pasar la peticion sin autenticar, y la regla de acceso de SecurityConfig
 * (con su AuthenticationEntryPoint) decide el rechazo. Asi el filtro tiene
 * una sola responsabilidad: identificar, no autorizar.
 *
 * SecurityConfig lo instancia dentro de la cadena (no es @Component ni
 * @Bean) para que Spring Boot no lo registre ademas como filtro servlet suelto.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final CargadorDeIdentidad cargadorDeIdentidad;

    public JwtAuthenticationFilter(JwtService jwtService, CargadorDeIdentidad cargadorDeIdentidad) {
        this.jwtService = jwtService;
        this.cargadorDeIdentidad = cargadorDeIdentidad;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = extraerToken(request);

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.esValido(token)) {
            autenticar(token, request);
        }

        chain.doFilter(request, response);
    }

    private void autenticar(String token, HttpServletRequest request) {
        try {
            UserDetails usuario = cargadorDeIdentidad.cargar(
                    jwtService.extraerDui(token), jwtService.extraerRol(token));

            if (!usuario.isEnabled()) {
                return;
            }

            var autenticacion = new UsernamePasswordAuthenticationToken(
                    usuario, null, usuario.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        } catch (UsernameNotFoundException e) {
            // El usuario del token ya no existe: se trata como no autenticado.
        }
    }

    private String extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO_BEARER)) {
            return null;
        }
        return cabecera.substring(PREFIJO_BEARER.length()).trim();
    }
}
