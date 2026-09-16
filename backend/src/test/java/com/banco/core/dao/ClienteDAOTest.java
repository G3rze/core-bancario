package com.banco.core.dao;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteDAOTest {

    @Test
    void guardaYRecuperaClientePorId(@TempDir Path tempDir) {
        ClienteDAO dao = new ClienteDAO(tempDir.resolve("clientes.dat"));
        Cliente cliente = new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

        dao.guardar(cliente);

        assertNotNull(cliente.getId());
        Optional<Cliente> recuperado = dao.buscarPorId(cliente.getId());
        assertTrue(recuperado.isPresent());
        assertEquals(cliente.getNumeroCliente(), recuperado.get().getNumeroCliente());
    }

    @Test
    void unNuevoDaoSobreElMismoArchivoVeLosRegistrosYaGuardados(@TempDir Path tempDir) {
        Path archivo = tempDir.resolve("clientes.dat");
        ClienteDAO dao = new ClienteDAO(archivo);
        dao.guardar(new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL));
        dao.guardar(new Cliente("98765432-1", "Luis Perez", "Santa Ana", "8888-8888", TipoCliente.NATURAL));

        List<Cliente> clientes = new ClienteDAO(archivo).listarTodos();

        assertEquals(2, clientes.size());
    }
}
