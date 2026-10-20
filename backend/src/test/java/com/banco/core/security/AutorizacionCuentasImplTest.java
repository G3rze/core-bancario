package com.banco.core.security;

import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.service.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutorizacionCuentasImplTest {

    private static final String DUI_DUENO = "12345678-9";
    private static final String DUI_OTRO = "98765432-1";

    private final CuentaService cuentas = mock(CuentaService.class);
    private final AutorizacionCuentasImpl autorizacion = new AutorizacionCuentasImpl(cuentas);

    private String numeroCuenta;

    private Authentication como(String dui, String rol) {
        return new UsernamePasswordAuthenticationToken(dui, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    @BeforeEach
    void cuentaDelDueno() {
        Cliente dueno = new Cliente(DUI_DUENO, "Ana Lopez", "San Salvador", "7777-7777", TipoCliente.NATURAL);
        Cuenta cuenta = new CuentaAhorros(dueno, new BigDecimal("100.00"));
        numeroCuenta = cuenta.getNumeroCuenta();
        when(cuentas.buscarPorNumero(numeroCuenta)).thenReturn(Optional.of(cuenta));
        when(cuentas.buscarPorNumero("AHO-999999")).thenReturn(Optional.empty());
    }

    @Test
    void clienteOperaSuPropiaCuenta() {
        assertTrue(autorizacion.puedeOperar(numeroCuenta, como(DUI_DUENO, "CLIENTE")));
    }

    @Test
    void clienteNoOperaLaCuentaDeOtro() {
        assertFalse(autorizacion.puedeOperar(numeroCuenta, como(DUI_OTRO, "CLIENTE")));
    }

    @Test
    void clienteSobreCuentaInexistenteRecibeFalseComoSiFueraAjena() {
        assertFalse(autorizacion.puedeOperar("AHO-999999", como(DUI_DUENO, "CLIENTE")));
    }

    @Test
    void cajeroYGerenteOperanCualquierCuentaIncluidaUnaInexistente() {
        assertTrue(autorizacion.puedeOperar(numeroCuenta, como(DUI_OTRO, "CAJERO")));
        assertTrue(autorizacion.puedeOperar(numeroCuenta, como(DUI_OTRO, "GERENTE")));
        assertTrue(autorizacion.puedeOperar("AHO-999999", como(DUI_OTRO, "CAJERO")));
    }

    @Test
    void adminNoOperaCuentasPorqueSoloGestionaUsuarios() {
        assertFalse(autorizacion.puedeOperar(numeroCuenta, como(DUI_OTRO, "ADMIN")));
    }

    @Test
    void sinAutenticarOAnonimoNoOpera() {
        Authentication anonimo = new AnonymousAuthenticationToken("k", "anonimo",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        assertFalse(autorizacion.puedeOperar(numeroCuenta, null));
        assertFalse(autorizacion.puedeOperar(numeroCuenta, anonimo));
    }
}
