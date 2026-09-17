package com.banco.core.model.dto;

import java.math.BigDecimal;

/**
 * {@code plazoMeses} solo aplica cuando {@code tipoCuenta} es un plazo fijo;
 * el Service es quien decide, segun {@code tipoCuenta}, que subtipo de
 * Cuenta instanciar.
 */
public record AperturaCuentaRequest(
        String duiTitular,
        String tipoCuenta,
        BigDecimal saldoInicial,
        Integer plazoMeses) {
}
