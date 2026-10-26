import { DecimalPipe, KeyValuePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { ReporteFinanciero } from '../../models/reporte.model';
import { ReporteService } from '../../services/reporte.service';

/** Reporte financiero del banco para el Gerente. */
@Component({
  selector: 'app-reporte-financiero',
  imports: [DecimalPipe, KeyValuePipe, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './reporte-financiero.html',
  styleUrl: './reporte-financiero.scss',
})
export class ReporteFinancieroComponent implements OnInit {
  private readonly reporteService = inject(ReporteService);

  protected readonly reporte = signal<ReporteFinanciero | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.reporteService.financiero().subscribe({
      next: (reporte) => {
        this.reporte.set(reporte);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.error ?? 'No fue posible generar el reporte');
      },
    });
  }
}
