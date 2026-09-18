package com.banco.core.service;

import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.Transaccion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de la logica de negocio de cuentas y transacciones. La
 * implementacion (paquete service.impl, a cargo del resto del equipo)
 * decide como mantener el historial ordenado (p. ej. TreeSet&lt;Transaccion&gt;)
 * y como notificar movimientos (p. ej. Queue&lt;Notificacion&gt;); este
 * contrato es lo unico que conoce CuentaController.
 */
public interface CuentaService {

    Cuenta abrirCuenta(String duiTitular, String tipoCuenta, BigDecimal saldoInicial, Integer plazoMeses);

    Optional<Cuenta> buscarPorNumero(String numeroCuenta);

    List<Cuenta> listarPorCliente(String duiTitular);

    Transaccion depositar(String numeroCuenta, BigDecimal monto);

    Transaccion retirar(String numeroCuenta, BigDecimal monto);

    Transaccion transferir(String numeroCuentaOrigen, String numeroCuentaDestino, BigDecimal monto);

    List<Transaccion> historial(String numeroCuenta);
}
