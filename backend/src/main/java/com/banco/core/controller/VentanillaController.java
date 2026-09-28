package com.banco.core.controller;

import com.banco.core.exception.ClienteNoEncontradoException;
import com.banco.core.exception.EmpleadoNoEncontradoException;
import com.banco.core.model.dto.ClienteConCuentasDTO;
import com.banco.core.model.dto.ClienteDTO;
import com.banco.core.model.dto.CuentaDTO;
import com.banco.core.model.dto.TransaccionDTO;
import com.banco.core.model.dto.VentanillaMovimientoRequest;
import com.banco.core.model.dto.VentanillaTransferenciaRequest;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Empleado;
import com.banco.core.service.EmpleadoService;
import com.banco.core.service.impl.ClienteServiceImpl;
import com.banco.core.service.impl.CuentaServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventanilla")
public class VentanillaController {

    private final ClienteServiceImpl clienteService;
    private final CuentaServiceImpl cuentaService;
    private final EmpleadoService empleadoService;

    public VentanillaController(
            ClienteServiceImpl clienteService,
            CuentaServiceImpl cuentaService,
            EmpleadoService empleadoService) {

        this.clienteService = clienteService;
        this.cuentaService = cuentaService;
        this.empleadoService = empleadoService;
    }

    @GetMapping("/clientes/{identificador}")
    public ClienteConCuentasDTO buscarCliente(@PathVariable String identificador) {

        var cliente = clienteService
                .buscarPorDuiONumeroCliente(identificador)
                .orElseThrow(() ->
                        ClienteNoEncontradoException.porDui(identificador));

        var cuentas = cuentaService.listarPorCliente(cliente.getDui())
                .stream()
                .map(CuentaDTO::desde)
                .toList();

        return new ClienteConCuentasDTO(
                ClienteDTO.desde(cliente),
                cuentas
        );
    }

    @PostMapping("/cuentas/{numeroCuenta}/depositos")
    public TransaccionDTO depositar(
            @PathVariable String numeroCuenta,
            @RequestBody VentanillaMovimientoRequest request) {

        Cajero cajero = resolverCajero(request.codigoEmpleadoCajero());

        return TransaccionDTO.desde(
                cuentaService.depositarEnVentanilla(
                        numeroCuenta,
                        request.monto(),
                        cajero
                )
        );
    }

    @PostMapping("/cuentas/{numeroCuenta}/retiros")
    public TransaccionDTO retirar(
            @PathVariable String numeroCuenta,
            @RequestBody VentanillaMovimientoRequest request) {

        Cajero cajero = resolverCajero(request.codigoEmpleadoCajero());

        return TransaccionDTO.desde(
                cuentaService.retirarEnVentanilla(
                        numeroCuenta,
                        request.monto(),
                        cajero
                )
        );
    }

    @PostMapping("/cuentas/{numeroCuenta}/transferencias")
    public TransaccionDTO transferir(
            @PathVariable String numeroCuenta,
            @RequestBody VentanillaTransferenciaRequest request) {

        Cajero cajero = resolverCajero(request.codigoEmpleadoCajero());

        return TransaccionDTO.desde(
                cuentaService.transferirEnVentanilla(
                        numeroCuenta,
                        request.numeroCuentaDestino(),
                        request.monto(),
                        cajero
                )
        );
    }

    private Cajero resolverCajero(String codigoEmpleado) {

        Empleado empleado = empleadoService
                .buscarPorCodigo(codigoEmpleado)
                .orElseThrow(() ->
                        EmpleadoNoEncontradoException.porCodigo(codigoEmpleado));

        if (!(empleado instanceof Cajero cajero)) {
            throw new IllegalArgumentException(
                    "El empleado indicado no es un cajero");
        }

        return cajero;
    }
}