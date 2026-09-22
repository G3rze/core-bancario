import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Cuenta } from '../models/cuenta.model';
import { Transaccion } from '../models/transaccion.model';
import { Cliente } from '../models/usuario.model';
import { AuthService } from './auth.service';

export interface ClienteConCuentas {
  cliente: Cliente;
  cuentas: Cuenta[];
}

/**
 * Flujo de caja/ventanilla (HU-007): busqueda de cliente por numero o DUI y
 * transacciones que un Cajero procesa a nombre de un cliente. A diferencia
 * de CuentaService (banca en linea, el cliente opera su propia cuenta),
 * cada operacion aqui va con el codigo del cajero logueado -por eso este
 * service depende de AuthService en vez de pedirle el codigo al
 * componente que lo llama-.
 */
@Injectable({ providedIn: 'root' })
export class VentanillaService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly baseUrl = `${environment.apiBaseUrl}/ventanilla`;

  buscarCliente(identificador: string): Observable<ClienteConCuentas> {
    return this.http.get<ClienteConCuentas>(`${this.baseUrl}/clientes/${identificador}`);
  }

  depositar(numeroCuenta: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/cuentas/${numeroCuenta}/depositos`, {
      monto,
      codigoEmpleadoCajero: this.codigoCajeroActual(),
    });
  }

  retirar(numeroCuenta: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/cuentas/${numeroCuenta}/retiros`, {
      monto,
      codigoEmpleadoCajero: this.codigoCajeroActual(),
    });
  }

  transferir(numeroCuenta: string, numeroCuentaDestino: string, monto: number): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.baseUrl}/cuentas/${numeroCuenta}/transferencias`, {
      numeroCuentaDestino,
      monto,
      codigoEmpleadoCajero: this.codigoCajeroActual(),
    });
  }

  private codigoCajeroActual(): string {
    const empleado = this.auth.empleadoActual();
    if (!empleado) {
      throw new Error('No hay un cajero autenticado');
    }
    return empleado.codigoEmpleado;
  }
}
