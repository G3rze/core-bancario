import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

/**
 * Adjunta el codigo del empleado logueado a cada request saliente. El login
 * de este avance (backend, ver CLAUDE.md) quedo deliberadamente minimo
 * -sin JWT ni sesiones server-side-, asi que el backend todavia no lee
 * este header; el interceptor deja el seam listo para ese hardening futuro
 * sin bloquear ninguna request hoy (si no hay sesion, deja pasar la
 * request tal cual).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const empleado = auth.empleadoActual();

  if (!empleado) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { 'X-Codigo-Empleado': empleado.codigoEmpleado },
    }),
  );
};
