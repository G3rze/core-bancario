package com.banco.core.model.dto;

import java.math.BigDecimal;

public record VentanillaTransferenciaRequest(String numeroCuentaDestino, BigDecimal monto,
                                               String codigoEmpleadoCajero) {
}
