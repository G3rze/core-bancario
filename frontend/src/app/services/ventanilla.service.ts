import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Cuenta } from '../models/cuenta.model';
import { Transaccion } from '../models/transaccion.model';
import { Cliente } from '../models/usuario.model';

export interface ClienteConCuentas {
  cliente: Cliente;
  cuentas: Cuenta[];
}

/**
 * Flujo de caja/ventanilla (HU-007): busqueda de cliente por numero o DUI y
 * transacciones que un Cajero procesa a nombre de un cliente. El cajero ya
 * no viaja en el body: el backend lo toma del token.
 */
@Injectable({ providedIn: 'root' })
export class VentanillaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/ventanilla`;

  buscarCliente(identificador: string): Observable<ClienteConCuentas> {
    return this.http.get<ClienteConCuentas>(`${this.baseUrl}/clientes/${identificador}`);
  }

  depositar(numeroCuenta: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/cuentas/${numeroCuenta}/depositos`, {
      monto,
    });
  }

  retirar(numeroCuenta: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/cuentas/${numeroCuenta}/retiros`, {
      monto,
    });
  }

  transferir(numeroCuenta: string, numeroCuentaDestino: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/cuentas/${numeroCuenta}/transferencias`, {
      numeroCuentaDestino,
      monto,
    });
  }
}
