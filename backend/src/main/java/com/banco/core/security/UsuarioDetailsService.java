package com.banco.core.security;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.EmpleadoRepository;
import com.banco.core.model.entity.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Reconstruye la identidad (DUI, rol, activo) de un usuario ya autenticado.
 * Las credenciales NO se validan aqui (eso lo hace el login, ver AuthService
 * en P3): por eso el password del UserDetails es una cadena vacia, que
 * nunca coincide con un hash BCrypt si alguien lo usara por error.
 *
 * Se consulta la base en cada request en vez de confiar en el claim del
 * token: si un usuario se desactiva o cambia de rol, el efecto es inmediato.
 *
 * El DUI es unico por tabla, no entre tablas: una persona puede ser
 * empleado y cliente a la vez. El token lleva el rol, y con el se sabe en
 * cual de las dos buscar ({@link #cargar(String, String)}).
 */
@Service
public class UsuarioDetailsService implements UserDetailsService, CargadorDeIdentidad {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    public UsuarioDetailsService(ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
    }

    /** Sin rol conocido: se prioriza al empleado (mayor privilegio de gestion) y luego al cliente. */
    @Override
    public UserDetails loadUserByUsername(String dui) throws UsernameNotFoundException {
        return buscarEmpleado(dui)
                .or(() -> buscarCliente(dui))
                .map(this::aUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + dui));
    }

    /** Carga al usuario de la tabla que corresponde al rol del token. */
    @Override
    public UserDetails cargar(String dui, String rol) throws UsernameNotFoundException {
        Optional<Usuario> usuario = ROL_CLIENTE.equals(rol) ? buscarCliente(dui) : buscarEmpleado(dui);
        return usuario
                .filter(u -> u.getRol().name().equals(rol))
                .map(this::aUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + dui));
    }

    private Optional<Usuario> buscarCliente(String dui) {
        return clienteRepository.findByDui(dui).map(Usuario.class::cast);
    }

    private Optional<Usuario> buscarEmpleado(String dui) {
        return empleadoRepository.findByDui(dui).map(Usuario.class::cast);
    }

    private UserDetails aUserDetails(Usuario usuario) {
        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getDui())
                .password("")
                .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
                .disabled(!usuario.isActivo())
                .build();
    }
}
