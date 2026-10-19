package com.banco.core.model.dto;

import com.banco.core.model.entity.TipoCliente;

/** Autoregistro de un cliente (HU-001): igual a NuevoClienteRequest mas la contrasena. */
public record RegistroRequest(
        String dui,
        String nombre,
        String direccion,
        String telefono,
        TipoCliente tipo,
        String password) {
}
