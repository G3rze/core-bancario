package com.banco.core.service;

import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.RegistroTransaccion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de la logica de negocio de cuentas y transacciones. La
 * implementacion decide como mantener el historial ordenado (TreeSet) y
 * como notificar movimientos (Queue); este contrato es lo unico que conoce
 * CuentaController. Devuelve RegistroTransaccion (no Transaccion): el
 * objeto de negocio vivo con referencias a Cuenta/Cajero es un detalle
 * interno de la implementacion, usado solo mientras se ejecuta la
 * transaccion (ver CuentaServiceImpl/RegistroTransaccion).
 */
public interface CuentaService {

    Cuenta abrirCuenta(String duiTitular, String tipoCuenta, BigDecimal saldoInicial, Integer plazoMeses);

    Optional<Cuenta> buscarPorNumero(String numeroCuenta);

    List<Cuenta> listarPorCliente(String duiTitular);

    RegistroTransaccion depositar(String numeroCuenta, BigDecimal monto);

    RegistroTransaccion retirar(String numeroCuenta, BigDecimal monto);

    RegistroTransaccion transferir(String numeroCuentaOrigen, String numeroCuentaDestino, BigDecimal monto);

    List<RegistroTransaccion> historial(String numeroCuenta);
}
