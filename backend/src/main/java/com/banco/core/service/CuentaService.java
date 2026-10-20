package com.banco.core.service;

import com.banco.core.model.dto.HistorialResumenDTO;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoCuenta;
import com.banco.core.model.entity.TipoTransaccion;

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

    Cuenta abrirCuenta(String duiTitular, TipoCuenta tipoCuenta, BigDecimal saldoInicial, Integer plazoMeses);

    Optional<Cuenta> buscarPorNumero(String numeroCuenta);

    List<Cuenta> listarPorCliente(String duiTitular);

    RegistroTransaccion depositar(String numeroCuenta, BigDecimal monto);

    RegistroTransaccion retirar(String numeroCuenta, BigDecimal monto);

    RegistroTransaccion transferir(String numeroCuentaOrigen, String numeroCuentaDestino, BigDecimal monto);

    List<RegistroTransaccion> historial(String numeroCuenta);

    /**
     * Variantes de ventanilla (HU-007): igual que depositar/retirar/transferir
     * pero dejan registrado el cajero que las proceso, que es obligatorio.
     */
    RegistroTransaccion depositarEnVentanilla(String numeroCuenta, BigDecimal monto, Cajero cajero);

    RegistroTransaccion retirarEnVentanilla(String numeroCuenta, BigDecimal monto, Cajero cajero);

    RegistroTransaccion transferirEnVentanilla(String numeroCuentaOrigen, String numeroCuentaDestino,
                                               BigDecimal monto, Cajero cajero);

    /** Historial de la cuenta; {@code tipo} nulo no filtra. */
    List<RegistroTransaccion> historialFiltrado(String numeroCuenta, TipoTransaccion tipo);

    /** Total de ingresos (depositos) y egresos (retiros y transferencias) de la cuenta. */
    HistorialResumenDTO resumenHistorial(String numeroCuenta);
}
