package com.banco.core.model.dto;

import com.banco.core.model.entity.Cliente;

public record ClienteDTO(
        String numeroCliente,
        String dui,
        String nombre,
        String direccion,
        String telefono,
        String tipo,
        boolean activo) {

    public static ClienteDTO desde(Cliente cliente) {
        return new ClienteDTO(
                cliente.getNumeroCliente(),
                cliente.getDui(),
                cliente.getNombre(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getTipo().name(),
                cliente.isActivo());
    }
}
