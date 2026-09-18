package com.banco.core.service;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la logica de negocio de clientes. La implementacion
 * (paquete service.impl, a cargo del resto del equipo) decide como
 * indexar/buscar clientes internamente (p. ej. Map&lt;String, Cliente&gt;
 * por DUI); este contrato es lo unico que conoce ClienteController.
 */
public interface ClienteService {

    Cliente registrar(String dui, String nombre, String direccion, String telefono, TipoCliente tipo);

    Optional<Cliente> buscarPorDui(String dui);

    List<Cliente> listarTodos();
}
