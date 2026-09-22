package com.banco.core.service.impl;

import com.banco.core.dao.TransaccionDAO;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Los tests con @SpringBootTest levantan el contexto completo, y
 * TransaccionDAO por defecto escribe en backend/datos/transacciones.dat -el
 * mismo archivo que usa `mvn spring-boot:run` en desarrollo-. Este bean
 * reemplaza esa ruta por un directorio temporal por corrida de test, para
 * no ensuciar los datos reales.
 */
@TestConfiguration
public class TransaccionDaoTestConfig {

    @Bean
    @Primary
    public TransaccionDAO transaccionDAO() throws IOException {
        return new TransaccionDAO(Files.createTempDirectory("core-bancario-test").resolve("transacciones.dat"));
    }
}
