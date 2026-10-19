package com.banco.core.controller;

import com.banco.core.model.dto.ClienteDTO;
import com.banco.core.model.dto.LoginRequest;
import com.banco.core.model.dto.LoginResponse;
import com.banco.core.model.dto.RegistroRequest;
import com.banco.core.service.AuthService;
import com.banco.core.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ClienteService clienteService;

    public AuthController(AuthService authService, ClienteService clienteService) {
        this.authService = authService;
        this.clienteService = clienteService;
    }

    /** Publico (HU-001). DUI repetido, formato invalido o password corta -> 400 {"error": ...}. */
    @PostMapping("/registro")
    public ResponseEntity<ClienteDTO> registro(@RequestBody RegistroRequest request) {
        var cliente = clienteService.registrar(request.dui(), request.nombre(), request.direccion(),
                request.telefono(), request.tipo(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.desde(cliente));
    }

    /** Publico (ver SecurityConfig). Credenciales invalidas -> 401 {"error": ...}. */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.dui(), request.password());
    }
}
