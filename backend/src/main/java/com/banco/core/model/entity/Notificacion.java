package com.banco.core.model.entity;

import java.time.LocalDateTime;

/**
 * Evento interno generado por cada transaccion ejecutada. El envio real
 * (push/SMS) esta fuera de alcance de este avance academico: este record
 * solo modela el dato que alimenta la Queue<Notificacion> de
 * CuentaServiceImpl (punto 6 de la rubrica, coleccion FIFO).
 */
public record Notificacion(String mensaje, LocalDateTime fecha) {

    public static Notificacion deTransaccion(Transaccion transaccion) {
        return new Notificacion(
                "%s: %s".formatted(transaccion.getClass().getSimpleName(), transaccion.generarComprobante()),
                LocalDateTime.now());
    }
}
