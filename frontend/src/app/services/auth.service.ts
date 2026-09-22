import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { Empleado, LoginRequest } from '../models/empleado.model';

const CLAVE_SESION = 'core-bancario.empleado';

/**
 * Sesion del empleado (Cajero/Gerente) logueado. No hay login de Cliente en
 * ninguna de las 7 historias de usuario de este avance, asi que este
 * service es exclusivo del flujo de ventanilla.
 * <p>
 * El estado vive en un signal (fuente de verdad para la UI, p. ej. los
 * guards) y se espeja en sessionStorage solo para sobrevivir un refresh de
 * la pagina dentro de la misma pestana -no localStorage: una terminal de
 * cajero no deberia quedar logueada indefinidamente en el navegador-.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly empleadoSignal = signal<Empleado | null>(this.leerSesionGuardada());

  readonly empleadoActual = this.empleadoSignal.asReadonly();

  login(request: LoginRequest): Observable<Empleado> {
    return this.http
      .post<Empleado>(`${this.baseUrl}/login`, request)
      .pipe(tap((empleado) => this.guardarSesion(empleado)));
  }

  logout(): void {
    this.empleadoSignal.set(null);
    try {
      sessionStorage.removeItem(CLAVE_SESION);
    } catch {
      // La sesion en memoria (el signal) ya se limpio; sessionStorage puede
      // no estar disponible (modo privado, etc.) sin que eso sea un error.
    }
  }

  private guardarSesion(empleado: Empleado): void {
    this.empleadoSignal.set(empleado);
    try {
      sessionStorage.setItem(CLAVE_SESION, JSON.stringify(empleado));
    } catch {
      // Si sessionStorage falla, la sesion sigue viva en memoria para esta
      // pestana; solo no sobrevive un refresh. No es un caso que debamos
      // frenar el login por el.
    }
  }

  private leerSesionGuardada(): Empleado | null {
    try {
      const guardado = sessionStorage.getItem(CLAVE_SESION);
      return guardado ? (JSON.parse(guardado) as Empleado) : null;
    } catch {
      return null;
    }
  }
}
