package com.banco.core.service.impl;

import com.banco.core.dao.EmpleadoRepository;
import com.banco.core.model.entity.Administrador;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Empleado;
import com.banco.core.model.entity.Gerente;
import com.banco.core.service.EmpleadoService;
import com.banco.core.util.PoliticaPassword;
import jakarta.annotation.PostConstruct;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void inicializarAlArrancar() {
        empleadoRepository.findAll().forEach(
                empleado -> Empleado.avanzarContador(empleado.getCodigoEmpleado())
        );
    }

    @Override
    @Transactional
    public Empleado registrarCajero(
            String dui,
            String nombre,
            String direccion,
            String telefono,
            String sucursal,
            String cajaAsignada,
            String password) {

        Cajero cajero = new Cajero(
                dui,
                nombre,
                direccion,
                telefono,
                sucursal,
                cajaAsignada,
                codificar(password)
        );

        try {
            return empleadoRepository.save(cajero);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Ya existe un empleado registrado con DUI " + dui,
                    e
            );
        }
    }

    @Override
    @Transactional
    public Empleado registrarGerente(
            String dui,
            String nombre,
            String direccion,
            String telefono,
            String sucursal,
            BigDecimal montoMaximoAprobacion,
            String password) {

        Gerente gerente = new Gerente(
                dui,
                nombre,
                direccion,
                telefono,
                sucursal,
                montoMaximoAprobacion,
                codificar(password)
        );

        try {
            return empleadoRepository.save(gerente);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Ya existe un empleado registrado con DUI " + dui,
                    e
            );
        }
    }

    @Override
    @Transactional
    public Empleado registrarAdministrador(
            String dui,
            String nombre,
            String direccion,
            String telefono,
            String sucursal,
            String password) {

        Administrador administrador = new Administrador(
                dui,
                nombre,
                direccion,
                telefono,
                sucursal,
                codificar(password)
        );

        try {
            return empleadoRepository.save(administrador);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Ya existe un empleado registrado con DUI " + dui,
                    e
            );
        }
    }

    @Override
    public Optional<Empleado> autenticar(String dui, String password) {
        return empleadoRepository.findByDui(dui)
                .filter(empleado -> empleado.validarPassword(password, passwordEncoder));
    }

    @Override
    public Optional<Empleado> buscarPorCodigo(String codigoEmpleado) {
        return empleadoRepository.findByCodigoEmpleado(codigoEmpleado);
    }

    /** Valida la politica sobre el texto plano y devuelve el hash BCrypt que guarda la entidad. */
    private String codificar(String password) {
        PoliticaPassword.validar(password);
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean existeAlgunGerente() {
        return empleadoRepository.contarGerentes() > 0;
    }
}