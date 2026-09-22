import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AperturaCuentaRequest, Cuenta } from '../models/cuenta.model';
import { MovimientoRequest, Transaccion, TransferenciaRequest } from '../models/transaccion.model';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/cuentas`;

  abrirCuenta(request: AperturaCuentaRequest): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.baseUrl, request);
  }

  buscarPorNumero(numeroCuenta: string): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.baseUrl}/${numeroCuenta}`);
  }

  listarPorCliente(duiTitular: string): Observable<Cuenta[]> {
    const params = new HttpParams().set('duiTitular', duiTitular);
    return this.http.get<Cuenta[]>(this.baseUrl, { params });
  }

  depositar(numeroCuenta: string, request: MovimientoRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/${numeroCuenta}/depositos`, request);
  }

  retirar(numeroCuenta: string, request: MovimientoRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/${numeroCuenta}/retiros`, request);
  }

  transferir(numeroCuenta: string, request: TransferenciaRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/${numeroCuenta}/transferencias`, request);
  }

  historial(numeroCuenta: string): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.baseUrl}/${numeroCuenta}/transacciones`);
  }
}
