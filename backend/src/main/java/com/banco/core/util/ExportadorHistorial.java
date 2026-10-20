package com.banco.core.util;

import com.banco.core.model.entity.RegistroTransaccion;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Convierte un historial en texto plano para descargar. Solo da formato: no
 * consulta ni filtra, eso lo hace el servicio.
 */
public final class ExportadorHistorial {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SIN_DATO = "-";

    private ExportadorHistorial() {
    }

    public static String aTexto(String numeroCuenta, List<RegistroTransaccion> registros) {
        StringBuilder texto = new StringBuilder();
        texto.append("Historial de la cuenta ").append(numeroCuenta).append('\n');
        texto.append("Movimientos: ").append(registros.size()).append('\n');
        texto.append("Fecha | Transaccion | Tipo | Monto | Saldo posterior | Estado\n");
        for (RegistroTransaccion r : registros) {
            texto.append(r.getFecha().format(FECHA)).append(" | ")
                    .append(r.getNumeroTransaccion()).append(" | ")
                    .append(r.getTipo()).append(" | ")
                    .append(r.getMonto()).append(" | ")
                    .append(saldo(r.saldoPosteriorDe(numeroCuenta))).append(" | ")
                    .append(r.getEstado()).append('\n');
        }
        return texto.toString();
    }

    /** Los registros anteriores a saldoPosterior no lo tienen: se muestra "-" en vez de inventarlo. */
    private static String saldo(BigDecimal saldo) {
        return saldo == null ? SIN_DATO : saldo.toPlainString();
    }
}
