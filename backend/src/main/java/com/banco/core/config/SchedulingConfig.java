package com.banco.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita @Scheduled en la app. Aparte para que CoreBancarioApplication no
 * cargue anotaciones de infraestructura que no son del arranque en si (ver
 * CorsConfig, mismo criterio).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
