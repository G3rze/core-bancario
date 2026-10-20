package com.banco.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Aparte de SecurityConfig a proposito: los servicios que codifican
 * contrasenas (registro, seeder) dependen solo de PasswordEncoder, y
 * SecurityConfig depende del filtro, que depende de repositorios. Con el
 * bean aqui no se forma un ciclo de dependencias.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
