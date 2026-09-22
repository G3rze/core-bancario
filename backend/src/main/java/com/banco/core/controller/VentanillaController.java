package com.banco.core.controller;

import com.banco.core.model.dto.ClienteConCuentasDTO;
import com.banco.core.model.dto.TransaccionDTO;
import com.banco.core.model.dto.VentanillaMovimientoRequest;
import com.banco.core.model.dto.VentanillaTransferenciaRequest;
import com.banco.core.service.EmpleadoService;
import com.banco.core.service.impl.ClienteServiceImpl;
import com.banco.core.service.impl.CuentaServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flujo de caja/ventanilla (HU-007): busqueda de cliente por numero o DUI,
 * sus cuentas, y las transacciones que un Cajero procesa a nombre de un
 * cliente -a diferencia de CuentaController, que es el cliente operando su
 * propia cuenta por banca en linea (sin cajero asociado)-.
 * <p>
 * TODO: implementar los 4 endpoints. Ya estan todos los
 * metodos que hacen falta del lado de los services inyectados abajo, esto
 * es orquestacion, no logica nueva:
 * <ul>
 *   <li>{@code clienteService.buscarPorDuiONumeroCliente(identificador)} ->
 *       si no esta, {@code ClienteNoEncontradoException.porDui(identificador)}.
 *   <li>{@code cuentaService.listarPorCliente(cliente.getDui())} + mapear
 *       cada Cuenta con {@code CuentaDTO.desde(...)} para armar el
 *       {@code ClienteConCuentasDTO} junto con {@code ClienteDTO.desde(cliente)}.
 *   <li>{@code cuentaService.depositarEnVentanilla/retirarEnVentanilla/
 *       transferirEnVentanilla(..., cajero)} -> envolver el resultado con
 *       {@code TransaccionDTO.desde(...)}.
 *   <li>Resolver el Cajero a partir de {@code request.codigoEmpleadoCajero()}
 *       con {@code empleadoService.buscarPorCodigo(...)} (si no esta,
 *       {@code EmpleadoNoEncontradoException.porCodigo(...)}), y validar
 *       que sea {@code instanceof Cajero} (si no, IllegalArgumentException).
 * </ul>
 * Mirar CuentaController para el patron de los otros 3 endpoints
 * (deposito/retiro/transferencia) que ya estan resueltos ahi, solo sin
 * cajero de por medio.
 */
@RestController
@RequestMapping("/api/ventanilla")
public class VentanillaController {

    private final ClienteServiceImpl clienteService;
    private final CuentaServiceImpl cuentaService;
    private final EmpleadoService empleadoService;

    public VentanillaController(ClienteServiceImpl clienteService, CuentaServiceImpl cuentaService,
                                 EmpleadoService empleadoService) {
        this.clienteService = clienteService;
        this.cuentaService = cuentaService;
        this.empleadoService = empleadoService;
    }

    @GetMapping("/clientes/{identificador}")
    public ClienteConCuentasDTO buscarCliente(@PathVariable String identificador) {
        throw new UnsupportedOperationException("TODO: ver comentario de la clase");
    }

    @PostMapping("/cuentas/{numeroCuenta}/depositos")
    public TransaccionDTO depositar(@PathVariable String numeroCuenta,
                                     @RequestBody VentanillaMovimientoRequest request) {
        throw new UnsupportedOperationException("TODO: ver comentario de la clase");
    }

    @PostMapping("/cuentas/{numeroCuenta}/retiros")
    public TransaccionDTO retirar(@PathVariable String numeroCuenta,
                                   @RequestBody VentanillaMovimientoRequest request) {
        throw new UnsupportedOperationException("TODO: ver comentario de la clase");
    }

    @PostMapping("/cuentas/{numeroCuenta}/transferencias")
    public TransaccionDTO transferir(@PathVariable String numeroCuenta,
                                      @RequestBody VentanillaTransferenciaRequest request) {
        throw new UnsupportedOperationException("TODO: ver comentario de la clase");
    }
}
