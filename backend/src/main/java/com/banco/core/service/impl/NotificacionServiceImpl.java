package com.banco.core.service.impl;

import com.banco.core.model.entity.Notificacion;
import com.banco.core.model.entity.Transaccion;
import com.banco.core.service.NotificacionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    // Queue<Notificacion>: buffer FIFO de eventos generados por las
    // transacciones. El envio real (push/SMS) esta fuera de alcance de este
    // avance; la cola solo demuestra el patron productor/consumidor, y el
    // orden estricto de entrada/salida es justo lo que Queue garantiza y
    // List/Set no.
    private final Queue<Notificacion> notificaciones = new ConcurrentLinkedQueue<>();

    @Override
    public void registrar(Transaccion transaccion) {
        notificaciones.add(Notificacion.deTransaccion(transaccion));
    }

    @Override
    public List<Notificacion> obtenerPendientes() {
        List<Notificacion> pendientes = new ArrayList<>();
        Notificacion siguiente;
        while ((siguiente = notificaciones.poll()) != null) {
            pendientes.add(siguiente);
        }
        return pendientes;
    }
}
