package com.banco.core.controller;

import com.banco.core.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.CannotCreateTransactionException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

// Si la base cae, el cliente recibe 503 con un mensaje generico: el detalle tecnico (host, SQL) nunca sale.
@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersistenciaExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void baseCaidaResponde503SinFiltrarElDetalleTecnico() throws Exception {
        when(clienteService.listarTodos())
                .thenThrow(new DataAccessResourceFailureException("connection refused: jdbc:postgresql://db:5432/core_bancario"));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(content().string(not(containsString("jdbc"))))
                .andExpect(content().string(not(containsString("connection refused"))));
    }

    @Test
    void noPoderAbrirLaTransaccionTambienResponde503() throws Exception {
        when(clienteService.listarTodos()).thenThrow(new CannotCreateTransactionException("no hay conexion"));

        mockMvc.perform(get("/api/clientes")).andExpect(status().isServiceUnavailable());
    }

    @Test
    void conflictoDeConcurrenciaResponde409() throws Exception {
        when(clienteService.listarTodos()).thenThrow(new OptimisticLockingFailureException("version obsoleta"));

        mockMvc.perform(get("/api/clientes")).andExpect(status().isConflict());
    }
}
