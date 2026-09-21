package com.banco.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * DataSource/JPA excluidos a proposito: spring-boot-starter-data-jpa y el
 * driver de Postgres estan en el pom.xml para el milestone futuro (ver
 * CLAUDE.md), pero todavia no hay datasource configurado — sin este
 * exclude, el contexto de Spring ni siquiera arranca.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class CoreBancarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBancarioApplication.class, args);
    }
}
