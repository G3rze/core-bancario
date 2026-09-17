package com.banco.core.model.dto;

import com.banco.core.model.entity.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionDTO(
        String numeroTransaccion,
        String tipo,
        BigDecimal monto,
        LocalDateTime fecha,
        String estado) {

    public static TransaccionDTO desde(Transaccion transaccion) {
        return new TransaccionDTO(
                transaccion.getNumeroTransaccion(),
                transaccion.getClass().getSimpleName(),
                transaccion.getMonto(),
                transaccion.getFecha(),
                transaccion.getEstado().name());
    }
}
