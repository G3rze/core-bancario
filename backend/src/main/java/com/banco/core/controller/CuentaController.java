package com.banco.core.controller;

import com.banco.core.exception.CuentaNoEncontradaException;
import com.banco.core.model.dto.AperturaCuentaRequest;
import com.banco.core.model.dto.CuentaDTO;
import com.banco.core.model.dto.HistorialResumenDTO;
import com.banco.core.model.dto.MovimientoRequest;
import com.banco.core.model.dto.TransaccionDTO;
import com.banco.core.model.dto.TransferenciaRequest;
import com.banco.core.security.AutorizacionCuentas;
import com.banco.core.model.entity.TipoCuenta;
import com.banco.core.service.CuentaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    // Regla de propiedad (ver security/AutorizacionCuentas): el cliente solo
    // opera sus cuentas; cajero y gerente cualquiera.
    private static final String PUEDE_OPERAR =
            "@autorizacionCuentas.puedeOperar(#numeroCuenta, authentication)";

    private final CuentaService cuentaService;

    private final AutorizacionCuentas autorizacion;

    public CuentaController(CuentaService cuentaService, AutorizacionCuentas autorizacion) {
        this.cuentaService = cuentaService;
        this.autorizacion = autorizacion;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','CAJERO')")
    public ResponseEntity<CuentaDTO> abrirCuenta(@RequestBody AperturaCuentaRequest request,
                                                 Authentication authentication) {
        // Un CLIENTE abre siempre a su nombre: se ignora request.duiTitular().
        String duiTitular = autorizacion.titularEfectivo(request.duiTitular(), authentication);
        var cuenta = cuentaService.abrirCuenta(duiTitular, TipoCuenta.desde(request.tipoCuenta()),
                request.saldoInicial(), request.plazoMeses());
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaDTO.desde(cuenta));
    }

    /** Cuentas del cliente autenticado (declarada antes que /{numeroCuenta}; ademas Spring prefiere la ruta literal). */
    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<CuentaDTO> misCuentas(Authentication authentication) {
        return cuentaService.listarPorCliente(authentication.getName()).stream().map(CuentaDTO::desde).toList();
    }

    @GetMapping("/{numeroCuenta}")
    @PreAuthorize(PUEDE_OPERAR)
    public CuentaDTO buscarPorNumero(@PathVariable String numeroCuenta) {
        return cuentaService.buscarPorNumero(numeroCuenta)
                .map(CuentaDTO::desde)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(numeroCuenta));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CAJERO','GERENTE')")
    public List<CuentaDTO> listarPorCliente(@RequestParam String duiTitular) {
        return cuentaService.listarPorCliente(duiTitular).stream().map(CuentaDTO::desde).toList();
    }

    @PostMapping("/{numeroCuenta}/depositos")
    @PreAuthorize(PUEDE_OPERAR)
    public TransaccionDTO depositar(@PathVariable String numeroCuenta, @RequestBody MovimientoRequest request) {
        return TransaccionDTO.desde(cuentaService.depositar(numeroCuenta, request.monto()));
    }

    @PostMapping("/{numeroCuenta}/retiros")
    @PreAuthorize(PUEDE_OPERAR)
    public TransaccionDTO retirar(@PathVariable String numeroCuenta, @RequestBody MovimientoRequest request) {
        return TransaccionDTO.desde(cuentaService.retirar(numeroCuenta, request.monto()));
    }

    @PostMapping("/{numeroCuenta}/transferencias")
    @PreAuthorize(PUEDE_OPERAR)
    public TransaccionDTO transferir(@PathVariable String numeroCuenta, @RequestBody TransferenciaRequest request) {
        return TransaccionDTO.desde(
                cuentaService.transferir(numeroCuenta, request.numeroCuentaDestino(), request.monto()));
    }

    @GetMapping("/{numeroCuenta}/transacciones")
    @PreAuthorize(PUEDE_OPERAR)
    public List<TransaccionDTO> historial(@PathVariable String numeroCuenta) {
        return cuentaService.historial(numeroCuenta).stream().map(TransaccionDTO::desde).toList();
    }

    @GetMapping("/{numeroCuenta}/transacciones/resumen")
    @PreAuthorize(PUEDE_OPERAR)
    public HistorialResumenDTO resumen(@PathVariable String numeroCuenta) {
        return cuentaService.resumenHistorial(numeroCuenta);
    }
}
