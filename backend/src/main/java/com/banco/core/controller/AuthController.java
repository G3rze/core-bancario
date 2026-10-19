package com.banco.core.controller;

import com.banco.core.model.dto.LoginRequest;
import com.banco.core.model.dto.LoginResponse;
import com.banco.core.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Publico (ver SecurityConfig). Credenciales invalidas -> 401 {"error": ...}. */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.dui(), request.password());
    }
}
