package com.banco.core.service.impl;

import com.banco.core.model.entity.Cuenta;
import com.banco.core.service.LimiteDiarioService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LimiteDiarioServiceImpl implements LimiteDiarioService {

    private final Map<String, BigDecimal> acumuladoDiario = new ConcurrentHashMap<>();
    private final Map<String, LocalDate> fechaAcumulado = new ConcurrentHashMap<>();

    @Override
    public void validar(Cuenta cuenta, BigDecimal monto) {
        LocalDate hoy = LocalDate.now();
        String numeroCuenta = cuenta.getNumeroCuenta();

        LocalDate ultimaFecha = fechaAcumulado.get(numeroCuenta);
        if (ultimaFecha == null || !ultimaFecha.equals(hoy)) {
            fechaAcumulado.put(numeroCuenta, hoy);
            acumuladoDiario.put(numeroCuenta, BigDecimal.ZERO);
        }

        BigDecimal nuevoAcumulado = acumuladoDiario.getOrDefault(numeroCuenta, BigDecimal.ZERO).add(monto);

        if (nuevoAcumulado.compareTo(cuenta.getLimiteRetiroDiario()) > 0) {
            throw new IllegalArgumentException("La operacion excede el limite diario permitido");
        }
    }

    @Override
    public void registrar(Cuenta cuenta, BigDecimal monto) {
        String numeroCuenta = cuenta.getNumeroCuenta();
        acumuladoDiario.merge(numeroCuenta, monto, BigDecimal::add);
        fechaAcumulado.put(numeroCuenta, LocalDate.now());
    }
}
