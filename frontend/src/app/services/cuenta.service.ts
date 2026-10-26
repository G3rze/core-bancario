import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AperturaCuentaRequest, Cuenta } from '../models/cuenta.model';
import {
  HistorialResumen,
  MovimientoRequest,
  TipoTransaccionFiltro,
  Transaccion,
  TransferenciaRequest,
} from '../models/transaccion.model';

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

  misCuentas(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.baseUrl}/mias`);
  }

  historial(numeroCuenta: string, tipo?: TipoTransaccionFiltro): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.baseUrl}/${numeroCuenta}/transacciones`, {
      params: this.paramsTipo(tipo),
    });
  }

  resumen(numeroCuenta: string): Observable<HistorialResumen> {
    return this.http.get<HistorialResumen>(`${this.baseUrl}/${numeroCuenta}/transacciones/resumen`);
  }

  /** Texto plano del historial; el componente decide como ofrecerlo como descarga. */
  exportar(numeroCuenta: string, tipo?: TipoTransaccionFiltro): Observable<string> {
    return this.http.get(`${this.baseUrl}/${numeroCuenta}/transacciones/exportar`, {
      params: this.paramsTipo(tipo),
      responseType: 'text',
    });
  }

  private paramsTipo(tipo?: TipoTransaccionFiltro): HttpParams {
    return tipo ? new HttpParams().set('tipo', tipo) : new HttpParams();
  }
}
