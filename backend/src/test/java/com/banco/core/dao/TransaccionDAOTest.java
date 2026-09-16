package com.banco.core.dao;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.Deposito;
import com.banco.core.model.entity.EstadoTransaccion;
import com.banco.core.model.entity.Transaccion;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransaccionDAOTest {

    @Test
    void guardaYRecuperaTransaccionYaEjecutada(@TempDir Path tempDir) {
        TransaccionDAO dao = new TransaccionDAO(tempDir.resolve("transacciones.dat"));
        Cliente titular = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        Cuenta cuenta = new CuentaAhorros(titular, new BigDecimal("100.00"));
        Transaccion deposito = new Deposito(cuenta, new BigDecimal("50.00"));
        deposito.ejecutar();

        dao.guardar(deposito);

        List<Transaccion> todas = dao.listarTodos();
        assertEquals(1, todas.size());
        assertEquals(EstadoTransaccion.COMPLETADA, todas.get(0).getEstado());
    }
}
