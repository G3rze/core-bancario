package com.banco.core.service;

import com.banco.core.model.dto.ReporteFinancieroDTO;

public interface ReporteService {

    /** Totales del banco al momento de la consulta. */
    ReporteFinancieroDTO generarReporteFinanciero();
}
