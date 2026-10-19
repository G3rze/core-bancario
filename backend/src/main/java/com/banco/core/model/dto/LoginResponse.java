package com.banco.core.model.dto;

import java.time.Instant;

/** {@code expiraEn} es un instante ISO-8601 en UTC (p. ej. "2026-10-19T22:00:00Z"). */
public record LoginResponse(String token, Instant expiraEn, UsuarioSesionDTO usuario) {
}
