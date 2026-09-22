package com.banco.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Hash de contrasenas con SHA-256 + salt aleatorio por password. No es el
 * estandar de la industria para passwords (para eso se usaria bcrypt/
 * Argon2, con costo configurable), pero evita el error mas basico -guardar
 * la contrasena en texto plano- sin sumar una libreria nueva solo para el
 * login minimo de este avance (no hay Spring Security en el proyecto).
 */
public final class PasswordHasher {

    private static final int LARGO_SALT = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[LARGO_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = hashear(password, salt);
        return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
    }

    public static boolean verificar(String password, String hashAlmacenado) {
        String[] partes = hashAlmacenado.split(":", 2);
        byte[] salt = HexFormat.of().parseHex(partes[0]);
        byte[] hashEsperado = HexFormat.of().parseHex(partes[1]);
        return MessageDigest.isEqual(hashEsperado, hashear(password, salt));
    }

    private static byte[] hashear(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
