import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Rol } from '../models/sesion.model';
import { AuthService } from '../services/auth.service';

/**
 * Guard por rol. Sin sesion manda a /login; con sesion pero sin el rol
 * requerido, a la pagina de inicio de su propio rol.
 */
export function rolGuard(rolesPermitidos: Rol[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const usuario = auth.usuarioActual();

    if (!usuario) {
      return router.createUrlTree(['/login']);
    }
    if (rolesPermitidos.includes(usuario.rol)) {
      return true;
    }
    return router.createUrlTree([auth.rutaInicio(usuario.rol)]);
  };
}
