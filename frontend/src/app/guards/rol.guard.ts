import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { RolEmpleado } from '../models/empleado.model';
import { AuthService } from '../services/auth.service';

/**
 * Guard por rol para las rutas de empleado (Cajero/Gerente). No hay login
 * de Cliente en ninguna de las 7 historias de usuario de este avance, asi
 * que las rutas de cliente (p. ej. /clientes) no usan este guard.
 */
export function rolGuard(rolesPermitidos: RolEmpleado[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const empleado = auth.empleadoActual();

    if (empleado && rolesPermitidos.includes(empleado.rol)) {
      return true;
    }
    return router.createUrlTree(['/login']);
  };
}
