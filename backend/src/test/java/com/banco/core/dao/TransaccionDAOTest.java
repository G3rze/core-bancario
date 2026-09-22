package com.banco.core.dao;

import com.banco.core.model.entity.RegistroTransaccion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransaccionDAOTest {

    @Test
    void guardaYRecuperaUnRegistroDeTransaccion(@TempDir Path tempDir) {
        TransaccionDAO dao = new TransaccionDAO(tempDir.resolve("transacciones.dat"));
        RegistroTransaccion registro = new RegistroTransaccion(
                "TRX-000001", "Deposito", new BigDecimal("50.00"), LocalDateTime.now(),
                "COMPLETADA", "AHO-000001", null, null);

        dao.guardar(registro);

        List<RegistroTransaccion> todas = dao.listarTodos();
        assertEquals(1, todas.size());
        assertEquals("COMPLETADA", todas.get(0).getEstado());
        assertEquals("AHO-000001", todas.get(0).getNumeroCuentaOrigen());
    }
}
