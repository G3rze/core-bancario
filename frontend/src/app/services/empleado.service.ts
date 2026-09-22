import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Empleado, NuevoEmpleadoRequest } from '../models/empleado.model';

@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/empleados`;

  registrar(request: NuevoEmpleadoRequest): Observable<Empleado> {
    return this.http.post<Empleado>(this.baseUrl, request);
  }
}
