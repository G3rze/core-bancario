package com.banco.core.service;

import com.banco.core.model.entity.Empleado;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Contrato de alta y autenticacion de empleados (Cajero/Gerente). La
 * implementacion decide como indexarlos internamente (Map por DUI y por
 * codigoEmpleado); este contrato es lo unico que conocen los controllers.
 */
public interface EmpleadoService {

    Empleado registrarCajero(String dui, String nombre, String direccion, String telefono, String sucursal,
                              String cajaAsignada, String password);

    Empleado registrarGerente(String dui, String nombre, String direccion, String telefono, String sucursal,
                               BigDecimal montoMaximoAprobacion, String password);

    Optional<Empleado> autenticar(String dui, String password);

    Optional<Empleado> buscarPorCodigo(String codigoEmpleado);

    /** Para el bootstrap: mientras no exista ningun Gerente, dar de alta empleados queda sin restriccion. */
    boolean existeAlgunGerente();
}
