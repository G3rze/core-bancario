package com.banco.core.controller;

import com.banco.core.exception.CuentaNoEncontradaException;
import com.banco.core.model.dto.AperturaCuentaRequest;
import com.banco.core.model.dto.CuentaDTO;
import com.banco.core.model.dto.MovimientoRequest;
import com.banco.core.model.dto.TransaccionDTO;
import com.banco.core.model.dto.TransferenciaRequest;
import com.banco.core.service.CuentaService;
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

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    public ResponseEntity<CuentaDTO> abrirCuenta(@RequestBody AperturaCuentaRequest request) {
        var cuenta = cuentaService.abrirCuenta(request.duiTitular(), request.tipoCuenta(),
                request.saldoInicial(), request.plazoMeses());
        return ResponseEntity.status(HttpStatus.CREATED).body(CuentaDTO.desde(cuenta));
    }

    @GetMapping("/{numeroCuenta}")
    public CuentaDTO buscarPorNumero(@PathVariable String numeroCuenta) {
        return cuentaService.buscarPorNumero(numeroCuenta)
                .map(CuentaDTO::desde)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(numeroCuenta));
    }

    @GetMapping
    public List<CuentaDTO> listarPorCliente(@RequestParam String duiTitular) {
        return cuentaService.listarPorCliente(duiTitular).stream().map(CuentaDTO::desde).toList();
    }

    @PostMapping("/{numeroCuenta}/depositos")
    public TransaccionDTO depositar(@PathVariable String numeroCuenta, @RequestBody MovimientoRequest request) {
        return TransaccionDTO.desde(cuentaService.depositar(numeroCuenta, request.monto()));
    }

    @PostMapping("/{numeroCuenta}/retiros")
    public TransaccionDTO retirar(@PathVariable String numeroCuenta, @RequestBody MovimientoRequest request) {
        return TransaccionDTO.desde(cuentaService.retirar(numeroCuenta, request.monto()));
    }

    @PostMapping("/{numeroCuenta}/transferencias")
    public TransaccionDTO transferir(@PathVariable String numeroCuenta, @RequestBody TransferenciaRequest request) {
        return TransaccionDTO.desde(
                cuentaService.transferir(numeroCuenta, request.numeroCuentaDestino(), request.monto()));
    }

    @GetMapping("/{numeroCuenta}/transacciones")
    public List<TransaccionDTO> historial(@PathVariable String numeroCuenta) {
        return cuentaService.historial(numeroCuenta).stream().map(TransaccionDTO::desde).toList();
    }
}
