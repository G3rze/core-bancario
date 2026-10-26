import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

/**
 * Adjunta el JWT (Authorization: Bearer) a cada request saliente. Un 401 con
 * sesion activa significa token vencido o invalido: se cierra la sesion y se
 * manda a /login. El 401 del propio login (credenciales malas) no cuenta,
 * ahi todavia no hay sesion.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();

  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && token) {
        auth.logout();
        router.navigateByUrl('/login');
      }
      return throwError(() => err);
    }),
  );
};
