package com.banco.core.service.impl;

import com.banco.core.dao.EmpleadoRepository;
import com.banco.core.model.entity.Empleado;
import com.banco.core.service.EmpleadoService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * TODO: completar la logica de negocio de empleados.
 * EmpleadoRepository ya tiene todo lo necesario (findByDui,
 * findByCodigoEmpleado, contarGerentes) — ver ClienteServiceImpl.java como
 * referencia del mismo patron: registrar() intenta repository.save(...) y
 * captura DataIntegrityViolationException (constraint UNIQUE de dui) para
 * relanzarla como IllegalArgumentException (-> 400 ya manejado por
 * NegocioExceptionHandler).
 */
@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @PostConstruct
    void inicializarAlArrancar() {
        // El id lo genera Postgres, pero codigoEmpleado sigue siendo un
        // AtomicLong en memoria; sin esto, un empleado nuevo tras
        // reiniciar la JVM podria repetir el numero de uno ya persistido.
        empleadoRepository.findAll().forEach(empleado -> Empleado.avanzarContador(empleado.getCodigoEmpleado()));
    }

    // TODO: construir un Cajero (ver el constructor de la clase
    // Cajero) y guardarlo con empleadoRepository.save(...), capturando
    // DataIntegrityViolationException como se describe arriba.
    @Override
    public Empleado registrarCajero(String dui, String nombre, String direccion, String telefono, String sucursal,
                                     String cajaAsignada, String password) {
        throw new UnsupportedOperationException("TODO: registrarCajero");
    }

    // TODO: igual que registrarCajero() pero construyendo un Gerente.
    @Override
    public Empleado registrarGerente(String dui, String nombre, String direccion, String telefono, String sucursal,
                                      BigDecimal montoMaximoAprobacion, String password) {
        throw new UnsupportedOperationException("TODO: registrarGerente");
    }

    // TODO: empleadoRepository.findByDui(dui), y si esta
    // presente, filtrar con empleado.validarPassword(password) (ya
    // implementado en la entidad Empleado, solo hay que llamarlo).
    @Override
    public Optional<Empleado> autenticar(String dui, String password) {
        throw new UnsupportedOperationException("TODO: autenticar");
    }

    // TODO: empleadoRepository.findByCodigoEmpleado(codigoEmpleado).
    @Override
    public Optional<Empleado> buscarPorCodigo(String codigoEmpleado) {
        throw new UnsupportedOperationException("TODO: buscarPorCodigo");
    }

    // TODO: empleadoRepository.contarGerentes() > 0.
    @Override
    public boolean existeAlgunGerente() {
        throw new UnsupportedOperationException("TODO: existeAlgunGerente");
    }
}
