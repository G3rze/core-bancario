package com.banco.core.util;

import com.banco.core.model.entity.RegistroTransaccion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Sello de ventanilla: codigo del cajero + 8 caracteres hexadecimales de un
 * SHA-256 sobre los datos del movimiento. Es determinista: recalcularlo con
 * el mismo registro da el mismo sello, y cambiar cualquier dato (monto,
 * cuenta, fecha...) lo cambia.
 *
 * Es una huella para mostrar y cotejar el comprobante impreso; no es una
 * firma: no usa clave secreta, asi que no prueba por si sola quien lo emitio.
 */
public final class SelloDigital {

    private static final int LARGO_HASH_HEX = 8;
    private static final String SEPARADOR = "|";

    private SelloDigital() {
    }

    /** null si el registro no paso por ventanilla (no tiene cajero). */
    public static String generar(RegistroTransaccion registro) {
        String cajero = registro.getCodigoEmpleadoCajero();
        if (cajero == null) {
            return null;
        }
        String datos = String.join(SEPARADOR,
                registro.getNumeroTransaccion(),
                registro.getTipo(),
                registro.getMonto().toPlainString(),
                registro.getNumeroCuentaOrigen(),
                String.valueOf(registro.getNumeroCuentaDestino()),
                cajero,
                registro.getFecha().toString());
        return cajero + "-" + hashCorto(datos);
    }

    private static String hashCorto(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash).substring(0, LARGO_HASH_HEX);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 es obligatorio en toda JVM; si falta, el entorno esta roto.
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
