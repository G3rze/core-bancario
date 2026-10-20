package com.banco.core.controller;

import com.banco.core.model.dto.EmpleadoDTO;
import com.banco.core.model.dto.NuevoEmpleadoRequest;
import com.banco.core.model.entity.Empleado;
import com.banco.core.service.EmpleadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    /**
     * Solo un GERENTE autenticado. El primer gerente lo crea el seeder
     * (config/BootstrapUsuariosRunner), ya no hay modo "abierto mientras no
     * exista gerente" ni cabecera X-Codigo-Empleado.
     */
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EmpleadoDTO> registrar(@RequestBody NuevoEmpleadoRequest request) {

        Empleado empleado;

        if ("CAJERO".equalsIgnoreCase(request.rol())) {

            empleado = empleadoService.registrarCajero(
                    request.dui(),
                    request.nombre(),
                    request.direccion(),
                    request.telefono(),
                    request.sucursal(),
                    request.cajaAsignada(),
                    request.password()
            );

        } else if ("GERENTE".equalsIgnoreCase(request.rol())) {

            empleado = empleadoService.registrarGerente(
                    request.dui(),
                    request.nombre(),
                    request.direccion(),
                    request.telefono(),
                    request.sucursal(),
                    request.montoMaximoAprobacion(),
                    request.password()
            );

        } else {

            throw new IllegalArgumentException(
                    "Rol de empleado invalido: " + request.rol());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EmpleadoDTO.desde(empleado));
    }
}