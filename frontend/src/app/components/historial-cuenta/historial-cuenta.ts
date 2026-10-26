import { DecimalPipe, DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';

import { HistorialResumen, TipoTransaccionFiltro, Transaccion } from '../../models/transaccion.model';
import { CuentaService } from '../../services/cuenta.service';

/** Historial de una cuenta (HU-006): filtro por tipo en servidor, resumen y exportar a .txt. */
@Component({
  selector: 'app-historial-cuenta',
  imports: [
    DecimalPipe,
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './historial-cuenta.html',
  styleUrl: './historial-cuenta.scss',
})
export class HistorialCuenta {
  readonly numeroCuenta = input.required<string>();
  // Cambia cada vez que el padre hizo una operacion, para refrescar.
  readonly version = input(0);

  private readonly cuentaService = inject(CuentaService);

  protected readonly columnas = ['fecha', 'tipo', 'monto', 'saldoPosterior', 'estado'];
  protected readonly transacciones = signal<Transaccion[]>([]);
  protected readonly resumen = signal<HistorialResumen | null>(null);
  protected readonly filtro = signal<TipoTransaccionFiltro | ''>('');
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      // Lee las senales para que el efecto se vuelva a ejecutar al cambiar cualquiera.
      this.version();
      this.cargar(this.numeroCuenta(), this.filtro());
    });
  }

  exportar(): void {
    const numero = this.numeroCuenta();
    const tipo = this.filtro() || undefined;
    this.cuentaService.exportar(numero, tipo).subscribe({
      next: (texto) => {
        const url = URL.createObjectURL(new Blob([texto], { type: 'text/plain;charset=utf-8' }));
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `historial-${numero}.txt`;
        enlace.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.error.set(err.error?.error ?? 'No se pudo exportar el historial'),
    });
  }

  private cargar(numero: string, filtro: TipoTransaccionFiltro | ''): void {
    this.cargando.set(true);
    this.error.set(null);
    this.cuentaService.historial(numero, filtro || undefined).subscribe({
      next: (transacciones) => {
        this.transacciones.set(transacciones);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.error ?? 'No fue posible cargar el historial');
      },
    });
    this.cuentaService.resumen(numero).subscribe({
      next: (resumen) => this.resumen.set(resumen),
      error: () => this.resumen.set(null),
    });
  }
}
