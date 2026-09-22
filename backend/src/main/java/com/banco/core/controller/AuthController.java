package com.banco.core.controller;

import com.banco.core.exception.CredencialesInvalidasException;
import com.banco.core.model.dto.EmpleadoDTO;
import com.banco.core.model.dto.LoginRequest;
import com.banco.core.service.EmpleadoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmpleadoService empleadoService;

    public AuthController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping("/login")
    public EmpleadoDTO login(@RequestBody LoginRequest request) {
        return empleadoService.autenticar(request.dui(), request.password())
                .map(EmpleadoDTO::desde)
                .orElseThrow(() -> new CredencialesInvalidasException("DUI o contrasena incorrectos"));
    }
}
