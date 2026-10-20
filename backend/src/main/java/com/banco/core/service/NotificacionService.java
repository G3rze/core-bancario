package com.banco.core.service;

import com.banco.core.model.entity.Notificacion;
import com.banco.core.model.entity.Transaccion;

import java.util.List;

/**
 * Cola de notificaciones generadas por las transacciones. El envio real
 * (push/SMS) esta fuera de alcance: la cola queda lista para que un
 * despachador la consuma.
 */
public interface NotificacionService {

    /** Encola la notificacion de una transaccion ya ejecutada. */
    void registrar(Transaccion transaccion);

    /** Drena la cola en orden FIFO: cada notificacion se entrega una sola vez. */
    List<Notificacion> obtenerPendientes();
}
