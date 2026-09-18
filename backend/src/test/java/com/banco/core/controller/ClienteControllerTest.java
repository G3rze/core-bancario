package com.banco.core.controller;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    private Cliente clientePrueba() {
        return new Cliente("12345678-9", "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
    }

    @Test
    void listarTodosDevuelveLosClientesDelService() throws Exception {
        when(clienteService.listarTodos()).thenReturn(List.of(clientePrueba()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dui").value("12345678-9"));
    }

    @Test
    void buscarPorDuiDevuelve404CuandoNoExiste() throws Exception {
        when(clienteService.buscarPorDui("00000000-0")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clientes/00000000-0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarDelegaEnElServiceYDevuelve201() throws Exception {
        Cliente cliente = clientePrueba();
        when(clienteService.registrar(any(), any(), any(), any(), any())).thenReturn(cliente);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dui":"12345678-9","nombre":"Ana Lopez","direccion":"San Salvador",\
                                "telefono":"7777-7777","tipo":"NATURAL"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCliente").value(cliente.getNumeroCliente()));
    }
}
