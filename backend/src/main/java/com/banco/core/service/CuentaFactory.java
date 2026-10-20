package com.banco.core.service;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.TipoCuenta;

import java.math.BigDecimal;

/**
 * Sabe construir cada subtipo de Cuenta. Separarlo de CuentaService evita que
 * el servicio conozca los constructores concretos: un tipo nuevo se agrega
 * aqui, no en la logica de apertura.
 */
public interface CuentaFactory {

    /**
     * @param plazoMeses solo lo usa PLAZO_FIJO; los demas tipos lo ignoran
     * @throws IllegalArgumentException si el tipo es nulo, no esta registrado o falta un dato que su tipo exige
     */
    Cuenta crear(TipoCuenta tipo, Cliente titular, BigDecimal saldoInicial, Integer plazoMeses);
}
