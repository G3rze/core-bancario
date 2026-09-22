import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';

import { Cuenta } from '../../models/cuenta.model';
import { Transaccion } from '../../models/transaccion.model';
import { ClienteConCuentas, VentanillaService } from '../../services/ventanilla.service';

type Operacion = 'DEPOSITO' | 'RETIRO' | 'TRANSFERENCIA';

@Component({
  selector: 'app-ventanilla',
  imports: [
    FormsModule,
    DecimalPipe,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatTableModule,
    MatIconModule,
  ],
  templateUrl: './ventanilla.html',
  styleUrl: './ventanilla.scss',
})
export class Ventanilla {
  private readonly ventanilla = inject(VentanillaService);
  private readonly snackBar = inject(MatSnackBar);

  protected identificador = '';
  protected monto: number | null = null;
  protected numeroCuentaDestino = '';

  protected readonly columnasCuenta = ['numeroCuenta', 'tipoCuenta', 'saldo', 'estado', 'acciones'];

  protected readonly resultado = signal<ClienteConCuentas | null>(null);
  protected readonly cuentaSeleccionada = signal<Cuenta | null>(null);
  protected readonly operacion = signal<Operacion | null>(null);
  protected readonly comprobante = signal<Transaccion | null>(null);
  protected readonly cargando = signal(false);

  buscar(): void {
    this.comprobante.set(null);
    this.cuentaSeleccionada.set(null);
    this.operacion.set(null);
    this.cargando.set(true);

    this.ventanilla.buscarCliente(this.identificador.trim()).subscribe({
      next: (encontrado) => {
        this.cargando.set(false);
        this.resultado.set(encontrado);
      },
      error: (err) => {
        this.cargando.set(false);
        this.resultado.set(null);
        this.mostrarError(err.error?.error ?? 'No se encontro el cliente');
      },
    });
  }

  seleccionarCuenta(cuenta: Cuenta): void {
    this.cuentaSeleccionada.set(cuenta);
    this.operacion.set(null);
    this.comprobante.set(null);
  }

  elegirOperacion(tipo: Operacion): void {
    this.operacion.set(tipo);
    this.comprobante.set(null);
    this.monto = null;
    this.numeroCuentaDestino = '';
  }

  confirmar(): void {
    const cuenta = this.cuentaSeleccionada();
    const tipo = this.operacion();
    if (!cuenta || !tipo || this.monto === null) {
      return;
    }

    this.cargando.set(true);

    const solicitud =
      tipo === 'DEPOSITO'
        ? this.ventanilla.depositar(cuenta.numeroCuenta, this.monto)
        : tipo === 'RETIRO'
          ? this.ventanilla.retirar(cuenta.numeroCuenta, this.monto)
          : this.ventanilla.transferir(cuenta.numeroCuenta, this.numeroCuentaDestino, this.monto);

    solicitud.subscribe({
      next: (transaccion) => {
        this.cargando.set(false);
        this.comprobante.set(transaccion);
        this.operacion.set(null);
        this.refrescarCliente();
      },
      error: (err) => {
        this.cargando.set(false);
        this.mostrarError(err.error?.error ?? 'No se pudo procesar la transaccion');
      },
    });
  }

  private refrescarCliente(): void {
    // El comprobante que devuelve el backend es de la transaccion, no de la
    // cuenta actualizada; se vuelve a buscar para traer el saldo al dia.
    this.ventanilla.buscarCliente(this.identificador.trim()).subscribe((encontrado) => {
      this.resultado.set(encontrado);
      const numeroCuenta = this.cuentaSeleccionada()?.numeroCuenta;
      this.cuentaSeleccionada.set(encontrado.cuentas.find((c) => c.numeroCuenta === numeroCuenta) ?? null);
    });
  }

  private mostrarError(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', { duration: 5000 });
  }
}
