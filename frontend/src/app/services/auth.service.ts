import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { Cliente } from '../models/usuario.model';
import { LoginRequest, RegistroRequest, Rol, Sesion } from '../models/sesion.model';

const CLAVE_SESION = 'core-bancario.sesion';

/**
 * Sesion del usuario logueado (Cliente, Cajero, Gerente o Admin) con el JWT
 * que devuelve el backend.
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

  private readonly sesionSignal = signal<Sesion | null>(this.leerSesionGuardada());

  readonly sesion = this.sesionSignal.asReadonly();
  readonly usuarioActual = computed(() => this.sesionSignal()?.usuario ?? null);
  readonly token = computed(() => this.sesionSignal()?.token ?? null);

  login(request: LoginRequest): Observable<Sesion> {
    return this.http
      .post<Sesion>(`${this.baseUrl}/login`, request)
      .pipe(tap((sesion) => this.guardarSesion(sesion)));
  }

  registrar(request: RegistroRequest): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.baseUrl}/registro`, request);
  }

  logout(): void {
    this.sesionSignal.set(null);
    try {
      sessionStorage.removeItem(CLAVE_SESION);
    } catch {
      // La sesion en memoria (el signal) ya se limpio; sessionStorage puede
      // no estar disponible (modo privado, etc.) sin que eso sea un error.
    }
  }

  /** Pagina de inicio segun el rol, para redirigir despues del login. */
  rutaInicio(rol: Rol): string {
    switch (rol) {
      case 'CLIENTE':
        return '/mis-cuentas';
      case 'CAJERO':
        return '/ventanilla';
      case 'GERENTE':
        return '/clientes';
      case 'ADMIN':
        return '/admin/usuarios';
    }
  }

  private guardarSesion(sesion: Sesion): void {
    this.sesionSignal.set(sesion);
    try {
      sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    } catch {
      // Si sessionStorage falla, la sesion sigue viva en memoria para esta
      // pestana; solo no sobrevive un refresh. No es un caso que debamos
      // frenar el login por el.
    }
  }

  private leerSesionGuardada(): Sesion | null {
    try {
      const guardado = sessionStorage.getItem(CLAVE_SESION);
      if (!guardado) {
        return null;
      }
      const sesion = JSON.parse(guardado) as Sesion;
      return new Date(sesion.expiraEn).getTime() > Date.now() ? sesion : null;
    } catch {
      return null;
    }
  }
}
