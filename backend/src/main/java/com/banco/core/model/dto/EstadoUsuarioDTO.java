package com.banco.core.model.dto;

import com.banco.core.model.entity.Usuario;

import java.util.List;

/**
 * Resultado de activar/desactivar. {@code roles} lista cada registro afectado:
 * el DUI es unico por tabla, asi que una persona que es cliente y empleado a
 * la vez aparece con dos roles.
 */
public record EstadoUsuarioDTO(String dui, boolean activo, List<String> roles) {

    public static EstadoUsuarioDTO desde(List<? extends Usuario> usuarios) {
        Usuario primero = usuarios.get(0);
        return new EstadoUsuarioDTO(primero.getDui(), primero.isActivo(),
                usuarios.stream().map(u -> u.getRol().name()).toList());
    }
}
