package com.banco.core.model.dto;

import java.math.BigDecimal;

public record HistorialResumenDTO(
        BigDecimal ingresos,
        BigDecimal egresos
) {
}
