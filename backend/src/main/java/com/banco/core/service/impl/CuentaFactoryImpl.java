package com.banco.core.service.impl;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.CuentaCorriente;
import com.banco.core.model.entity.CuentaPlazoFijo;
import com.banco.core.model.entity.TipoCuenta;
import com.banco.core.service.CuentaFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registro de "creadores" por tipo. Los tres tipos actuales se registran al
 * construir; {@link #registrar} permite reemplazar o agregar uno sin tocar
 * CuentaServiceImpl (por ejemplo, en un test).
 */
@Component
public class CuentaFactoryImpl implements CuentaFactory {

    /** Receta para construir un tipo de cuenta; plazoMeses puede ser null. */
    @FunctionalInterface
    public interface Creador {
        Cuenta crear(Cliente titular, BigDecimal saldoInicial, Integer plazoMeses);
    }

    private final Map<TipoCuenta, Creador> creadores = new EnumMap<>(TipoCuenta.class);

    public CuentaFactoryImpl() {
        registrar(TipoCuenta.AHORROS, (titular, saldo, plazo) -> new CuentaAhorros(titular, saldo));
        registrar(TipoCuenta.CORRIENTE, (titular, saldo, plazo) -> new CuentaCorriente(titular, saldo));
        registrar(TipoCuenta.PLAZO_FIJO, (titular, saldo, plazo) -> {
            if (plazo == null) {
                throw new IllegalArgumentException("Una cuenta a plazo fijo requiere un plazo en meses");
            }
            return new CuentaPlazoFijo(titular, saldo, plazo);
        });
    }

    public void registrar(TipoCuenta tipo, Creador creador) {
        creadores.put(tipo, creador);
    }

    @Override
    public Cuenta crear(TipoCuenta tipo, Cliente titular, BigDecimal saldoInicial, Integer plazoMeses) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio");
        }
        Creador creador = creadores.get(tipo);
        if (creador == null) {
            throw new IllegalArgumentException("Tipo de cuenta no soportado: " + tipo);
        }
        return creador.crear(titular, saldoInicial, plazoMeses);
    }
}
