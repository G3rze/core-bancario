package com.banco.core.dao;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuentaDAOTest {

    @Test
    void guardaYRecuperaCuentaConSuTitular(@TempDir Path tempDir) {
        CuentaDAO dao = new CuentaDAO(tempDir.resolve("cuentas.dat"));
        Cliente titular = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        Cuenta cuenta = new CuentaAhorros(titular, new BigDecimal("100.00"));

        dao.guardar(cuenta);

        Optional<Cuenta> recuperada = dao.buscarPorId(cuenta.getId());
        assertTrue(recuperada.isPresent());
        assertEquals(cuenta.getNumeroCuenta(), recuperada.get().getNumeroCuenta());
        assertEquals(0, new BigDecimal("100.00").compareTo(recuperada.get().getSaldo()));
    }
}
