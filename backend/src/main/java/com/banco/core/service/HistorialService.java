package com.banco.core.service;

import com.banco.core.model.dto.HistorialResumenDTO;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.TipoTransaccion;

import java.util.List;

/**
 * Historial de movimientos por cuenta, en orden cronologico (HU-006). No
 * verifica que la cuenta exista: eso lo decide quien lo usa (CuentaService).
 */
public interface HistorialService {

    /** Agrega el registro al historial de cada cuenta que involucra. */
    void indexar(RegistroTransaccion registro);

    /** Copia del historial de la cuenta; vacio si no tiene movimientos. */
    List<RegistroTransaccion> listar(String numeroCuenta);

    /** Como {@link #listar}, solo del tipo indicado; {@code tipo} nulo no filtra. */
    List<RegistroTransaccion> filtrar(String numeroCuenta, TipoTransaccion tipo);

    /** Total de ingresos (depositos) y egresos (retiros y transferencias) de la cuenta. */
    HistorialResumenDTO resumir(String numeroCuenta);
}
