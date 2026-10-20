package com.banco.core.model.dto;

/** Wrapper Boolean (no boolean): permite detectar que el campo no vino en el JSON. */
public record CambioEstadoRequest(Boolean activo) {
}
