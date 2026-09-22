package com.banco.core.model.dto;

import com.banco.core.model.entity.RegistroTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionDTO(
        String numeroTransaccion,
        String tipo,
        BigDecimal monto,
        LocalDateTime fecha,
        String estado,
        String codigoEmpleadoCajero) {

    public static TransaccionDTO desde(RegistroTransaccion registro) {
        return new TransaccionDTO(
                registro.getNumeroTransaccion(),
                registro.getTipo(),
                registro.getMonto(),
                registro.getFecha(),
                registro.getEstado(),
                registro.getCodigoEmpleadoCajero());
    }
}
