package com.banco.core.model.dto;

import java.math.BigDecimal;

public record TransferenciaRequest(String numeroCuentaDestino, BigDecimal monto) {
}
