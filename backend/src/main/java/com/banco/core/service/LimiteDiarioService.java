package com.banco.core.service;

import com.banco.core.model.entity.Cuenta;

import java.math.BigDecimal;

/**
 * Control del monto retirado o transferido por cuenta en el dia (HU-004 /
 * HU-005). El acumulado vive en memoria y se reinicia al cambiar de dia.
 *
 * No es atomico por si solo: quien lo usa debe llamar a validar() y
 * registrar() dentro de la misma seccion critica de la cuenta.
 */
public interface LimiteDiarioService {

    /**
     * @throws IllegalArgumentException si acumulado de hoy + monto supera el limite diario de la cuenta
     */
    void validar(Cuenta cuenta, BigDecimal monto);

    /** Suma el monto al acumulado de hoy; llamar solo despues de que el movimiento se ejecuto. */
    void registrar(Cuenta cuenta, BigDecimal monto);
}
