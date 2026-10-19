package com.banco.core.config;

import com.banco.core.security.CargadorDeIdentidad;
import com.banco.core.security.JwtAuthenticationFilter;
import com.banco.core.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Solo conecta piezas: sesion sin estado, que rutas son publicas y donde va
 * el filtro JWT. La identificacion la hace JwtAuthenticationFilter y la
 * autorizacion por rol/propiedad se declara en los controllers con
 * @PreAuthorize (@EnableMethodSecurity), no aqui.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService,
                                           CargadorDeIdentidad cargadorDeIdentidad) throws Exception {

        // El filtro se crea aqui y no como @Bean: un Filter que es bean lo
        // registra Spring Boot tambien como filtro servlet suelto.
        var filtroJwt = new JwtAuthenticationFilter(jwtService, cargadorDeIdentidad);

        http
                // API con Bearer token y sin cookies de sesion: no hay sesion
                // que un tercero pueda aprovechar, asi que CSRF no aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // Sin esto, Spring responde 403 a quien no se identifico; la
                // semantica correcta es 401. El cuerpo {"error":...} llega en P6.
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
