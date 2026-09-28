import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CuentaService } from '../../services/cuenta.service';

@Component({
  selector: 'app-transferencia',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './transferencia.html',
  styleUrl: './transferencia.scss',
})
export class Transferencia {
  private readonly cuentaService = inject(CuentaService);
  private readonly snackBar = inject(MatSnackBar);

  protected cuentaOrigen = '';
  protected cuentaDestino = '';
  protected monto = 0;

  protected readonly cargando = signal(false);

  transferir(): void {

    this.cargando.set(true);

    this.cuentaService.transferir(
      this.cuentaOrigen,
      {
        numeroCuentaDestino: this.cuentaDestino,
        monto: this.monto,
      }
    ).subscribe({
      next: (trx) => {

        this.cargando.set(false);

        this.snackBar.open(
          `Transferencia realizada: ${trx.numeroTransaccion}`,
          'Cerrar',
          { duration: 4000 }
        );

        this.cuentaOrigen = '';
        this.cuentaDestino = '';
        this.monto = 0;
      },
      error: (err) => {

        this.cargando.set(false);

        this.snackBar.open(
          err.error?.error ?? 'No fue posible realizar la transferencia',
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }
}