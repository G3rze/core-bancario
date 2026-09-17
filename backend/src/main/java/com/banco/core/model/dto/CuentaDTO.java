package com.banco.core.model.dto;

import com.banco.core.model.entity.Cuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaDTO(
        String numeroCuenta,
        String tipoCuenta,
        BigDecimal saldo,
        String estado,
        LocalDateTime fechaApertura,
        String numeroClienteTitular) {

    public static CuentaDTO desde(Cuenta cuenta) {
        return new CuentaDTO(
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldo(),
                cuenta.getEstado().name(),
                cuenta.getFechaApertura(),
                cuenta.getTitular().getNumeroCliente());
    }
}
