package com.banco.core.service;

import com.banco.core.model.entity.Usuario;

import java.util.List;

/** Administracion de cuentas de usuario (hoy solo activar/desactivar). */
public interface UsuarioAdminService {

    /**
     * Activa o desactiva a la persona con ese DUI en todas las tablas donde
     * exista (cliente y/o empleado).
     *
     * @param duiSolicitante quien pide el cambio; no puede desactivarse a si mismo
     * @throws com.banco.core.exception.UsuarioNoEncontradoException si el DUI no existe
     * @throws IllegalArgumentException si el solicitante intenta desactivarse
     */
    List<Usuario> cambiarEstado(String dui, boolean activo, String duiSolicitante);
}
