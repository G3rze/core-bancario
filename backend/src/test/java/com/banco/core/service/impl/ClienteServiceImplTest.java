package com.banco.core.service.impl;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Transactional en la clase: cada @Test corre en su propia transaccion,
 * revertida al final -aisla los datos entre tests sin tener que inventar un
 * DUI distinto por metodo, y sin necesidad de limpiar la tabla a mano-.
 */
@Testcontainers
@SpringBootTest
@Import(TransaccionDaoTestConfig.class)
@Transactional
class ClienteServiceImplTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ClienteServiceImpl clienteService;

    @Test
    void registrarPersisteYPermiteBuscarPorDui() {
        Cliente cliente = clienteService.registrar("12345678-9", "Ana Lopez", "San Salvador", "7777-7777",
                TipoCliente.NATURAL);

        assertEquals("12345678-9", cliente.getDui());
        Optional<Cliente> recuperado = clienteService.buscarPorDui("12345678-9");
        assertTrue(recuperado.isPresent());
        assertEquals(cliente.getNumeroCliente(), recuperado.get().getNumeroCliente());
    }

    @Test
    void registrarConDuiDuplicadoLanzaExcepcion() {
        clienteService.registrar("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

        assertThrows(IllegalArgumentException.class, () -> clienteService.registrar(
                "12345678-9", "Otro Nombre", "Santa Ana", "8888-8888", TipoCliente.NATURAL));
    }

    @Test
    void listarTodosDevuelveOrdenadoPorNumeroDeCliente() {
        clienteService.registrar("98765432-1", "Luis Perez", "Santa Ana", "8888-8888", TipoCliente.NATURAL);
        clienteService.registrar("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);

        List<Cliente> clientes = clienteService.listarTodos();

        assertEquals(2, clientes.size());
        assertTrue(clientes.get(0).getNumeroCliente().compareTo(clientes.get(1).getNumeroCliente()) < 0);
    }

    @Test
    void buscarPorDuiONumeroClienteEncuentraPorCualquieraDeLosDos() {
        Cliente cliente = clienteService.registrar("12345678-9", "Ana Lopez", "San Salvador", "7777-7777",
                TipoCliente.NATURAL);

        assertTrue(clienteService.buscarPorDuiONumeroCliente(cliente.getDui()).isPresent());
        assertTrue(clienteService.buscarPorDuiONumeroCliente(cliente.getNumeroCliente()).isPresent());
        assertTrue(clienteService.buscarPorDuiONumeroCliente("no-existe").isEmpty());
    }
}
