package com.banco.core.security;

import com.banco.core.service.CuentaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Nombre explicito "autorizacionCuentas": asi lo referencian las expresiones
 * @PreAuthorize("@autorizacionCuentas.puedeOperar(#numeroCuenta, authentication)").
 * Depende de la interfaz CuentaService, no de repositorios.
 */
@Component("autorizacionCuentas")
public class AutorizacionCuentasImpl implements AutorizacionCuentas {

    private static final Set<String> ROLES_PERSONAL = Set.of("ROLE_CAJERO", "ROLE_GERENTE");
    private static final String ROL_CLIENTE = "ROLE_CLIENTE";

    private final CuentaService cuentaService;

    public AutorizacionCuentasImpl(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Override
    public boolean puedeOperar(String numeroCuenta, Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            return false;
        }
        // El personal pasa siempre: si la cuenta no existe, el controller responde 404.
        if (tieneAlgunRol(autenticacion, ROLES_PERSONAL)) {
            return true;
        }
        if (!tieneAlgunRol(autenticacion, Set.of(ROL_CLIENTE))) {
            return false;
        }
        // Cuenta inexistente y cuenta ajena dan la misma respuesta (403) para
        // que un cliente no pueda averiguar que numeros de cuenta existen.
        return cuentaService.buscarPorNumero(numeroCuenta)
                .map(cuenta -> cuenta.getTitular().getDui().equals(autenticacion.getName()))
                .orElse(false);
    }

    @Override
    public String titularEfectivo(String duiSolicitado, Authentication autenticacion) {
        if (tieneAlgunRol(autenticacion, Set.of(ROL_CLIENTE))) {
            return autenticacion.getName();
        }
        if (duiSolicitado == null || duiSolicitado.isBlank()) {
            throw new IllegalArgumentException("El DUI del titular es obligatorio");
        }
        return duiSolicitado;
    }

    private boolean tieneAlgunRol(Authentication autenticacion, Set<String> roles) {
        for (GrantedAuthority autoridad : autenticacion.getAuthorities()) {
            if (roles.contains(autoridad.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
