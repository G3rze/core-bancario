package com.banco.core.service.impl;

import com.banco.core.exception.CredencialesInvalidasException;
import com.banco.core.model.dto.LoginResponse;
import com.banco.core.model.dto.UsuarioSesionDTO;
import com.banco.core.model.entity.Usuario;
import com.banco.core.security.JwtService;
import com.banco.core.service.AuthService;
import com.banco.core.service.ClienteService;
import com.banco.core.service.EmpleadoService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Una sola responsabilidad: verificar credenciales y emitir el token. Depende
 * de las interfaces de servicio y de JwtService, no de repositorios.
 *
 * El DUI es unico por tabla, no entre clientes y empleados, y el login solo
 * recibe DUI y contrasena: se prueba primero como empleado y luego como
 * cliente, cada uno con su propia contrasena. Si ambas coincidieran, gana el
 * empleado.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String MENSAJE_INVALIDAS = "DUI o contrasena incorrectos";

    private final EmpleadoService empleadoService;
    private final ClienteService clienteService;
    private final JwtService jwtService;

    public AuthServiceImpl(EmpleadoService empleadoService, ClienteService clienteService,
                           JwtService jwtService) {
        this.empleadoService = empleadoService;
        this.clienteService = clienteService;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(String dui, String password) {
        Usuario usuario = verificar(dui, password)
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_INVALIDAS));

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("El usuario esta desactivado");
        }

        String token = jwtService.generarToken(usuario.getDui(), usuario.getRol().name());
        Instant expiraEn = Instant.now().plusMillis(jwtService.getExpiracionMs());
        return new LoginResponse(token, expiraEn, UsuarioSesionDTO.desde(usuario));
    }

    private Optional<? extends Usuario> verificar(String dui, String password) {
        if (dui == null || password == null) {
            return Optional.empty();
        }
        Optional<? extends Usuario> empleado = empleadoService.autenticar(dui, password);
        return empleado.isPresent() ? empleado : clienteService.autenticar(dui, password);
    }
}
