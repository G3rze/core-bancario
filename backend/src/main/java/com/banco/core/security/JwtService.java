package com.banco.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Unico lugar que conoce la libreria jjwt: genera y valida el token. El
 * filtro de autenticacion y el login dependen de esta clase y no de jjwt,
 * asi que cambiar de libreria (o de algoritmo) no los toca.
 *
 * El token lleva el DUI como subject y el rol como claim; no guarda
 * password ni datos personales.
 */
@Service
public class JwtService {

    private static final int LARGO_MINIMO_SECRETO = 32;
    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(
            @Value("${banco.jwt.secret}") String secreto,
            @Value("${banco.jwt.expiration-ms}") long expiracionMs) {

        // HS256 exige >= 256 bits; fallar al arrancar es mejor que firmar
        // con una clave debil sin darnos cuenta.
        if (secreto == null || secreto.length() < LARGO_MINIMO_SECRETO) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos " + LARGO_MINIMO_SECRETO + " caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String dui, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(dui)
                .claim(CLAIM_ROL, rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Vigencia configurada, para que el login informe {@code expiraEn}. */
    public long getExpiracionMs() {
        return expiracionMs;
    }

    /** true solo si la firma es correcta y el token no ha expirado. */
    public boolean esValido(String token) {
        try {
            leerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Lanza JwtException si el token es invalido; llamar tras {@link #esValido}. */
    public String extraerDui(String token) {
        return leerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return leerClaims(token).get(CLAIM_ROL, String.class);
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
