package com.banco.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Los rechazos de la cadena de seguridad ocurren en filtros, antes de llegar
 * al MVC, asi que los @RestControllerAdvice no los ven. Esta clase da a esos
 * 401 y 403 el mismo cuerpo {"error": "..."} que el resto de la API.
 *
 * 401 = no se identifico (sin token, invalido, vencido o usuario desactivado).
 * 403 = se identifico pero su rol o propiedad no alcanza.
 */
public class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    static final String MENSAJE_401 = "Autenticacion requerida o token invalido";
    static final String MENSAJE_403 = "No tiene permisos para realizar esta accion";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED, MENSAJE_401);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, MENSAJE_403);
    }

    private void escribir(HttpServletResponse response, HttpStatus estado, String mensaje) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // El mensaje es fijo: nunca refleja datos de la peticion ni de la excepcion.
        response.getWriter().write(mapper.writeValueAsString(Map.of("error", mensaje)));
    }
}
