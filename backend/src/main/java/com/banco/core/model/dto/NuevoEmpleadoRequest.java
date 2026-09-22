package com.banco.core.model.dto;

import java.math.BigDecimal;

/**
 * {@code rol} decide que subtipo de Empleado instancia el service
 * ("CAJERO" o "GERENTE"); {@code cajaAsignada} solo aplica a CAJERO y
 * {@code montoMaximoAprobacion} solo a GERENTE, igual que
 * {@code AperturaCuentaRequest.plazoMeses} solo aplica a plazo fijo.
 */
public record NuevoEmpleadoRequest(
        String dui,
        String nombre,
        String direccion,
        String telefono,
        String sucursal,
        String rol,
        String cajaAsignada,
        BigDecimal montoMaximoAprobacion,
        String password) {
}
