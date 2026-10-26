import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Ruta por defecto: manda a cada rol a su pagina de inicio, o a /login si no hay sesion. */
export const inicioGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const usuario = auth.usuarioActual();

  return router.createUrlTree([usuario ? auth.rutaInicio(usuario.rol) : '/login']);
};
