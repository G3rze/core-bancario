package com.banco.core.service.impl;

import com.banco.core.model.dto.HistorialResumenDTO;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoTransaccion;
import com.banco.core.service.HistorialService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HistorialServiceImpl implements HistorialService {

    private static final Comparator<RegistroTransaccion> ORDEN_CRONOLOGICO =
            Comparator.comparing(RegistroTransaccion::getFecha).thenComparing(RegistroTransaccion::getNumeroTransaccion);

    // Map<numeroCuenta, TreeSet<RegistroTransaccion>>: el historial de una
    // cuenta debe mostrarse en orden cronologico (HU-006); TreeSet mantiene
    // ese orden en cada insercion en vez de tener que ordenar toda la lista
    // en cada consulta. Sigue en memoria (no hay tabla de transacciones en
    // Postgres, transacciones.dat es la unica fuente persistida) porque
    // reconstruirlo leyendo el .dat completo en cada request seria mas caro
    // que mantenerlo actualizado incrementalmente.
    private final Map<String, TreeSet<RegistroTransaccion>> historialPorCuenta = new ConcurrentHashMap<>();

    @Override
    public void indexar(RegistroTransaccion registro) {
        registro.numerosCuentaInvolucradas().forEach(numeroCuenta -> {
            TreeSet<RegistroTransaccion> historial =
                    historialPorCuenta.computeIfAbsent(numeroCuenta, k -> new TreeSet<>(ORDEN_CRONOLOGICO));
            synchronized (historial) {
                historial.add(registro);
            }
        });
    }

    @Override
    public List<RegistroTransaccion> listar(String numeroCuenta) {
        TreeSet<RegistroTransaccion> historial = historialPorCuenta.get(numeroCuenta);
        if (historial == null) {
            return List.of();
        }
        synchronized (historial) {
            return List.copyOf(historial);
        }
    }

    @Override
    public List<RegistroTransaccion> filtrar(String numeroCuenta, TipoTransaccion tipo) {
        return listar(numeroCuenta).stream()
                .filter(registro -> tipo == null || registro.getTipoTransaccion() == tipo)
                .toList();
    }

    @Override
    public HistorialResumenDTO resumir(String numeroCuenta) {
        List<RegistroTransaccion> movimientos = listar(numeroCuenta);

        BigDecimal ingresos = sumar(movimientos, true);
        BigDecimal egresos = sumar(movimientos, false);

        return new HistorialResumenDTO(ingresos, egresos);
    }

    private BigDecimal sumar(List<RegistroTransaccion> movimientos, boolean ingreso) {
        return movimientos.stream()
                .filter(registro -> registro.getTipoTransaccion().esIngreso() == ingreso)
                .map(RegistroTransaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
