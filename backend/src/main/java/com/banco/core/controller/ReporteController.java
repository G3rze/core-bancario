package com.banco.core.controller;

import com.banco.core.model.dto.ReporteFinancieroDTO;
import com.banco.core.service.ReporteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reportes financieros: solo el Gerente (Entregable II, seccion 5). */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('GERENTE')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/financiero")
    public ReporteFinancieroDTO financiero() {
        return reporteService.generarReporteFinanciero();
    }
}
