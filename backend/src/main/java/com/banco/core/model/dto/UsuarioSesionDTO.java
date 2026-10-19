package com.banco.core.model.dto;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Empleado;
import com.banco.core.model.entity.Usuario;

/**
 * Quien inicio sesion, para que el frontend decida menu y rutas. Solo uno de
 * {@code codigoEmpleado} / {@code numeroCliente} viene lleno (el otro es null).
 */
public record UsuarioSesionDTO(
        String dui,
        String nombre,
        String rol,
        String codigoEmpleado,
        String numeroCliente) {

    public static UsuarioSesionDTO desde(Usuario usuario) {
        String codigoEmpleado = usuario instanceof Empleado e ? e.getCodigoEmpleado() : null;
        String numeroCliente = usuario instanceof Cliente c ? c.getNumeroCliente() : null;
        return new UsuarioSesionDTO(usuario.getDui(), usuario.getNombre(), usuario.getRol().name(),
                codigoEmpleado, numeroCliente);
    }
}
