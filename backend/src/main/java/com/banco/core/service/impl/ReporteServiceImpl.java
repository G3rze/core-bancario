package com.banco.core.service.impl;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.CuentaRepository;
import com.banco.core.dao.TransaccionDAO;
import com.banco.core.model.dto.ReporteFinancieroDTO;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.EstadoTransaccion;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final TransaccionDAO transaccionDAO;

    public ReporteServiceImpl(ClienteRepository clienteRepository, CuentaRepository cuentaRepository,
                              TransaccionDAO transaccionDAO) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.transaccionDAO = transaccionDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteFinancieroDTO generarReporteFinanciero() {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        List<RegistroTransaccion> transacciones = transaccionDAO.listarTodos();

        // TreeMap: orden estable por nombre para que el reporte se vea igual en cada consulta.
        Map<String, Long> cuentasPorTipo = cuentas.stream()
                .collect(Collectors.groupingBy(Cuenta::getTipoCuenta, TreeMap::new, Collectors.counting()));
        BigDecimal saldoTotal = cuentas.stream().map(Cuenta::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Solo cuentan las completadas en los montos; las rechazadas se informan aparte.
        Map<String, BigDecimal> montoPorTipo = transacciones.stream()
                .filter(t -> EstadoTransaccion.COMPLETADA.name().equals(t.getEstado()))
                .collect(Collectors.groupingBy(RegistroTransaccion::getTipo, TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, RegistroTransaccion::getMonto, BigDecimal::add)));
        long rechazadas = transacciones.stream()
                .filter(t -> EstadoTransaccion.RECHAZADA.name().equals(t.getEstado())).count();

        return new ReporteFinancieroDTO(clienteRepository.count(), cuentas.size(), cuentasPorTipo, saldoTotal,
                transacciones.size(), rechazadas, montoPorTipo);
    }
}
