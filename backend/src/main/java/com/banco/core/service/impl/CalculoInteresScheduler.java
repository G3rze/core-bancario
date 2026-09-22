package com.banco.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Hilo periodico que capitaliza el interes de las cuentas de ahorro
 * (justificacion de concurrencia, punto 5 de la rubrica): en un banco real
 * el interes se acredita en un ciclo fijo (mensual en la practica, no en
 * cada request), sin que un administrador tenga que dispararlo a mano por
 * cada cuenta. Se modela con @Scheduled en vez de un Thread suelto porque
 * Spring ya administra ese hilo (pool, logging de errores, arranque/apagado
 * junto con el contexto) sin codigo de infraestructura adicional.
 * <p>
 * El intervalo es configurable (banco.intereses.periodo-ms, default 24h)
 * para poder bajarlo en una demo y ver la capitalizacion en vivo sin
 * esperar un dia real.
 */
@Component
public class CalculoInteresScheduler {

    private static final Logger log = LoggerFactory.getLogger(CalculoInteresScheduler.class);

    private final CuentaServiceImpl cuentaService;

    public CalculoInteresScheduler(CuentaServiceImpl cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Scheduled(fixedRateString = "${banco.intereses.periodo-ms:86400000}")
    public void capitalizarIntereses() {
        log.info("Iniciando capitalizacion periodica de intereses");
        cuentaService.aplicarInteresPeriodico();
        log.info("Capitalizacion periodica de intereses finalizada");
    }
}
