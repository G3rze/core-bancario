package com.banco.core.controller;

import com.banco.core.exception.ClienteNoEncontradoException;
import com.banco.core.model.dto.ClienteDTO;
import com.banco.core.model.dto.NuevoClienteRequest;
import com.banco.core.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> registrar(@RequestBody NuevoClienteRequest request) {
        var cliente = clienteService.registrar(request.dui(), request.nombre(), request.direccion(),
                request.telefono(), request.tipo());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.desde(cliente));
    }

    @GetMapping("/{dui}")
    public ClienteDTO buscarPorDui(@PathVariable String dui) {
        return clienteService.buscarPorDui(dui)
                .map(ClienteDTO::desde)
                .orElseThrow(() -> ClienteNoEncontradoException.porDui(dui));
    }

    @GetMapping
    public List<ClienteDTO> listarTodos() {
        return clienteService.listarTodos().stream().map(ClienteDTO::desde).toList();
    }
}
