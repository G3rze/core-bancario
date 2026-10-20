package com.banco.core.controller;

import com.banco.core.model.dto.CambioEstadoRequest;
import com.banco.core.model.dto.EstadoUsuarioDTO;
import com.banco.core.service.UsuarioAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioAdminService usuarioAdminService;

    public AdminUsuarioController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @PatchMapping("/{dui}/estado")
    public EstadoUsuarioDTO cambiarEstado(@PathVariable String dui,
                                          @RequestBody CambioEstadoRequest request,
                                          Authentication authentication) {
        if (request.activo() == null) {
            throw new IllegalArgumentException("El campo activo es obligatorio");
        }
        return EstadoUsuarioDTO.desde(
                usuarioAdminService.cambiarEstado(dui, request.activo(), authentication.getName()));
    }
}
