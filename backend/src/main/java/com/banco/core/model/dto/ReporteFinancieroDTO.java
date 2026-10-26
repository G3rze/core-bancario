package com.banco.core.model.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Resumen financiero para el Gerente: totales de clientes, cuentas y movimientos del banco. */
public record ReporteFinancieroDTO(
        long totalClientes,
        long totalCuentas,
        Map<String, Long> cuentasPorTipo,
        BigDecimal saldoTotal,
        long totalTransacciones,
        long transaccionesRechazadas,
        Map<String, BigDecimal> montoPorTipoTransaccion) {
}
