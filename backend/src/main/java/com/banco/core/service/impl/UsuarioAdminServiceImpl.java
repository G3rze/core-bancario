package com.banco.core.service.impl;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.EmpleadoRepository;
import com.banco.core.exception.UsuarioNoEncontradoException;
import com.banco.core.model.entity.Usuario;
import com.banco.core.service.UsuarioAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioAdminServiceImpl implements UsuarioAdminService {

    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    public UsuarioAdminServiceImpl(ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    @Transactional
    public List<Usuario> cambiarEstado(String dui, boolean activo, String duiSolicitante) {
        // Si un admin se desactivara a si mismo podria dejar al sistema sin
        // nadie que pueda reactivar usuarios.
        if (!activo && dui != null && dui.equals(duiSolicitante)) {
            throw new IllegalArgumentException("Un administrador no puede desactivarse a si mismo");
        }

        List<Usuario> afectados = new ArrayList<>();
        empleadoRepository.findByDui(dui).ifPresent(afectados::add);
        clienteRepository.findByDui(dui).ifPresent(afectados::add);

        if (afectados.isEmpty()) {
            throw UsuarioNoEncontradoException.porDui(dui);
        }

        // La entidad decide como cambia su estado (activar/desactivar); aqui
        // solo se persiste. Los objetos son gestionados por JPA: el commit
        // de la transaccion guarda el cambio.
        afectados.forEach(u -> {
            if (activo) {
                u.activar();
            } else {
                u.desactivar();
            }
        });
        return afectados;
    }
}
