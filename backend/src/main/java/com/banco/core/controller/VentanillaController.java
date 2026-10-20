package com.banco.core.controller;

import com.banco.core.exception.AccesoNoAutorizadoException;
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
import com.banco.core.service.ClienteService;
import com.banco.core.service.CuentaService;
import com.banco.core.service.EmpleadoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventanilla")
@PreAuthorize("hasAnyRole('CAJERO','GERENTE')")
public class VentanillaController {

    // Un movimiento de ventanilla queda firmado por un Cajero (Transaccion
    // guarda un Cajero), asi que el GERENTE solo consulta; no mueve dinero.
    private static final String SOLO_CAJERO = "hasRole('CAJERO')";

    private final ClienteService clienteService;
    private final CuentaService cuentaService;
    private final EmpleadoService empleadoService;

    public VentanillaController(
            ClienteService clienteService,
            CuentaService cuentaService,
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
    @PreAuthorize(SOLO_CAJERO)
    public TransaccionDTO depositar(
            @PathVariable String numeroCuenta,
            @RequestBody VentanillaMovimientoRequest request,
            Authentication authentication) {

        Cajero cajero = resolverCajero(authentication);

        return TransaccionDTO.desde(
                cuentaService.depositarEnVentanilla(
                        numeroCuenta,
                        request.monto(),
                        cajero
                )
        );
    }

    @PostMapping("/cuentas/{numeroCuenta}/retiros")
    @PreAuthorize(SOLO_CAJERO)
    public TransaccionDTO retirar(
            @PathVariable String numeroCuenta,
            @RequestBody VentanillaMovimientoRequest request,
            Authentication authentication) {

        Cajero cajero = resolverCajero(authentication);

        return TransaccionDTO.desde(
                cuentaService.retirarEnVentanilla(
                        numeroCuenta,
                        request.monto(),
                        cajero
                )
        );
    }

    @PostMapping("/cuentas/{numeroCuenta}/transferencias")
    @PreAuthorize(SOLO_CAJERO)
    public TransaccionDTO transferir(
            @PathVariable String numeroCuenta,
            @RequestBody VentanillaTransferenciaRequest request,
            Authentication authentication) {

        Cajero cajero = resolverCajero(authentication);

        return TransaccionDTO.desde(
                cuentaService.transferirEnVentanilla(
                        numeroCuenta,
                        request.numeroCuentaDestino(),
                        request.monto(),
                        cajero
                )
        );
    }

    /**
     * El cajero sale del token, no del body: asi nadie puede registrar un
     * movimiento a nombre de otro empleado.
     */
    private Cajero resolverCajero(Authentication authentication) {

        String dui = authentication.getName();

        Empleado empleado = empleadoService
                .buscarPorDui(dui)
                .orElseThrow(() -> EmpleadoNoEncontradoException.porDui(dui));

        if (!(empleado instanceof Cajero cajero)) {
            throw new AccesoNoAutorizadoException(
                    "Solo un cajero puede operar en ventanilla");
        }

        return cajero;
    }
}
