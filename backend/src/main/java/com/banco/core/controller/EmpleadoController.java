package com.banco.core.controller;

import com.banco.core.exception.AccesoNoAutorizadoException;
import com.banco.core.model.dto.EmpleadoDTO;
import com.banco.core.model.dto.NuevoEmpleadoRequest;
import com.banco.core.model.entity.Empleado;
import com.banco.core.model.entity.Gerente;
import com.banco.core.service.EmpleadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public ResponseEntity<EmpleadoDTO> registrar(
            @RequestBody NuevoEmpleadoRequest request,
            @RequestHeader(value = "X-Codigo-Empleado", required = false)
            String codigoSolicitante) {

        if (empleadoService.existeAlgunGerente()) {

            Empleado solicitante = empleadoService
                    .buscarPorCodigo(codigoSolicitante)
                    .orElseThrow(() ->
                            new AccesoNoAutorizadoException(
                                    "Solo un gerente puede registrar empleados"));

            if (!(solicitante instanceof Gerente)) {
                throw new AccesoNoAutorizadoException(
                        "Solo un gerente puede registrar empleados");
            }
        }

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