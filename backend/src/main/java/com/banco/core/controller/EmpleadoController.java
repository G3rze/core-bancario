package com.banco.core.controller;

import com.banco.core.model.dto.EmpleadoDTO;
import com.banco.core.model.dto.NuevoEmpleadoRequest;
import com.banco.core.service.EmpleadoService;
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

    /**
     * TODO: dar de alta un Cajero o Gerente segun
     * request.rol(), restringido a que lo pida un Gerente ya registrado.
     * <p>
     * 1. Segun request.rol(): "CAJERO" -&gt; empleadoService.registrarCajero(...);
     *    "GERENTE" -&gt; empleadoService.registrarGerente(...); cualquier otro
     *    valor (o null) -&gt; IllegalArgumentException (-&gt; 400, ya manejado
     *    por NegocioExceptionHandler).
     * 2. Antes de crear el empleado, verificar autorizacion con el header
     *    "X-Codigo-Empleado" (ya viene como parametro aqui abajo):
     *      - Si empleadoService.existeAlgunGerente() es false, no hay
     *        restriccion todavia (bootstrap: el primer Gerente se crea
     *        libre, si no nadie podria crearlo nunca).
     *      - Si ya existe algun Gerente, el codigo del header debe
     *        resolver (empleadoService.buscarPorCodigo(...)) a un Gerente
     *        (instanceof Gerente); si no, lanzar
     *        AccesoNoAutorizadoException (-&gt; 403, ver el TODO en
     *        SeguridadExceptionHandler para agregar el @ExceptionHandler
     *        que le da ese status).
     * 3. Devolver 201 con EmpleadoDTO.desde(empleado).
     * <p>
     * Casos a cubrir en el test (WebMvcTest mockeando EmpleadoService):
     * sin Gerentes aun / sin header habiendo ya un Gerente / con header de
     * un Cajero / con header de un Gerente valido.
     */
    @PostMapping
    public ResponseEntity<EmpleadoDTO> registrar(@RequestBody NuevoEmpleadoRequest request,
                                                  @RequestHeader(value = "X-Codigo-Empleado", required = false)
                                                  String codigoSolicitante) {
        throw new UnsupportedOperationException("TODO: registrar empleado, ver comentario arriba");
    }
}
