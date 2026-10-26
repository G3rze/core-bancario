import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

// Alineado a com.banco.core.model.dto.EstadoUsuarioDTO (backend).
export interface EstadoUsuario {
  dui: string;
  activo: boolean;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin/usuarios`;

  cambiarEstado(dui: string, activo: boolean): Observable<EstadoUsuario> {
    return this.http.patch<EstadoUsuario>(`${this.baseUrl}/${dui}/estado`, { activo });
  }
}
