import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ReporteFinanciero } from '../models/reporte.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/reportes`;

  financiero(): Observable<ReporteFinanciero> {
    return this.http.get<ReporteFinanciero>(`${this.baseUrl}/financiero`);
  }
}
