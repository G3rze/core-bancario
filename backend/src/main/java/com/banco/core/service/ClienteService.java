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

    /**
     * Autoregistro (HU-001): igual que el alta sin contrasena pero el cliente
     * queda con credenciales y puede iniciar sesion.
     */
    Cliente registrar(String dui, String nombre, String direccion, String telefono, TipoCliente tipo,
                      String password);

    Optional<Cliente> buscarPorDui(String dui);

    List<Cliente> listarTodos();

    /** Cliente cuyo DUI y contrasena coinciden; vacio si no existe o la clave no corresponde. */
    Optional<Cliente> autenticar(String dui, String password);
}
