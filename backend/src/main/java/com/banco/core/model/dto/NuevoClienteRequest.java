package com.banco.core.model.dto;

import com.banco.core.model.entity.TipoCliente;

public record NuevoClienteRequest(
        String dui,
        String nombre,
        String direccion,
        String telefono,
        TipoCliente tipo) {
}
