import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import { Cuenta } from '../../models/cuenta.model';
import { Transaccion } from '../../models/transaccion.model';
import { CuentaService } from '../../services/cuenta.service';
import { HistorialCuenta } from '../historial-cuenta/historial-cuenta';

type Operacion = 'DEPOSITO' | 'RETIRO' | 'TRANSFERENCIA';

/**
 * Pantalla centrada en UNA cuenta (HU-003 a HU-006): el numero llega por la
 * ruta, asi que nadie lo teclea. Depositar, retirar y transferir comparten
 * formulario y muestran el comprobante que devuelve el backend.
 */
@Component({
  selector: 'app-cuenta-detalle',
  imports: [
    FormsModule,
    DecimalPipe,
    RouterLink,
    MatCardModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    HistorialCuenta,
  ],
  templateUrl: './cuenta-detalle.html',
  styleUrl: './cuenta-detalle.scss',
})
export class CuentaDetalle implements OnInit {
  // Parametro de ruta :numero (withComponentInputBinding).
  readonly numero = input.required<string>();

  private readonly cuentaService = inject(CuentaService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly cuenta = signal<Cuenta | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly comprobante = signal<Transaccion | null>(null);
  protected readonly cargando = signal(false);
  // Se incrementa tras cada operacion para que el historial se vuelva a pedir.
  protected readonly version = signal(0);

  protected monto: number | null = null;
  protected numeroCuentaDestino = '';

  ngOnInit(): void {
    this.cargarCuenta();
  }

  confirmar(operacion: Operacion, formulario: NgForm): void {
    if (this.monto === null) {
      return;
    }
    const numero = this.numero();
    let solicitud: Observable<Transaccion>;
    switch (operacion) {
      case 'DEPOSITO':
        solicitud = this.cuentaService.depositar(numero, { monto: this.monto });
        break;
      case 'RETIRO':
        solicitud = this.cuentaService.retirar(numero, { monto: this.monto });
        break;
      case 'TRANSFERENCIA':
        solicitud = this.cuentaService.transferir(numero, {
          numeroCuentaDestino: this.numeroCuentaDestino.trim(),
          monto: this.monto,
        });
        break;
    }

    this.cargando.set(true);
    solicitud.subscribe({
      next: (transaccion) => {
        this.cargando.set(false);
        this.comprobante.set(transaccion);
        formulario.resetForm();
        this.version.update((v) => v + 1);
        this.cargarCuenta();
      },
      error: (err) => {
        this.cargando.set(false);
        this.snackBar.open(err.error?.error ?? 'No se pudo procesar la operacion', 'Cerrar', { duration: 5000 });
      },
    });
  }

  limpiarComprobante(): void {
    this.comprobante.set(null);
  }

  private cargarCuenta(): void {
    this.cuentaService.buscarPorNumero(this.numero()).subscribe({
      next: (cuenta) => this.cuenta.set(cuenta),
      error: (err) => this.error.set(err.error?.error ?? 'No fue posible cargar la cuenta'),
    });
  }
}
