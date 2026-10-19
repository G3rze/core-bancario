package com.banco.core.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Lo unico que el filtro JWT necesita saber de quien carga usuarios: dado el
 * DUI y el rol del token, devolver su identidad actual. El filtro depende de
 * este contrato minimo y no de UsuarioDetailsService (ni de los repositorios).
 */
public interface CargadorDeIdentidad {

    UserDetails cargar(String dui, String rol) throws UsernameNotFoundException;
}
