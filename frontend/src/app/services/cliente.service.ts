import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Cliente, NuevoClienteRequest } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/clientes`;

  registrar(request: NuevoClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.baseUrl, request);
  }

  buscarPorDui(dui: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/${dui}`);
  }

  listarTodos(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.baseUrl);
  }
}
