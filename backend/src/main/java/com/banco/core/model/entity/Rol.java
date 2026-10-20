package com.banco.core.model.entity;

/**
 * Roles del sistema. Cada subtipo de Usuario declara el suyo en getRol();
 * el nombre del enum es lo que viaja en el token y en el JSON ("CAJERO"),
 * y con prefijo ROLE_ lo que usa Spring Security.
 */
public enum Rol {
    CLIENTE,
    CAJERO,
    GERENTE,
    ADMIN
}
