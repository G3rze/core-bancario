package com.banco.core.model.dto;

import java.math.BigDecimal;

public record VentanillaMovimientoRequest(BigDecimal monto, String codigoEmpleadoCajero) {
}
