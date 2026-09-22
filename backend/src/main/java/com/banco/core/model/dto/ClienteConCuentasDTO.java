package com.banco.core.model.dto;

import java.util.List;

/**
 * Respuesta de la busqueda de ventanilla (HU-007: buscar cliente por numero
 * o DUI y mostrar todas sus cuentas de una vez).
 */
public record ClienteConCuentasDTO(ClienteDTO cliente, List<CuentaDTO> cuentas) {
}
