import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

import { CuentaService } from '../../services/cuenta.service';

@Component({
  selector: 'app-deposito',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './deposito.html',
  styleUrl: './deposito.scss',
})
export class Deposito {
  private readonly cuentaService = inject(CuentaService);
  private readonly snackBar = inject(MatSnackBar);

  protected numeroCuenta = '';
  protected monto = 0;

  protected readonly cargando = signal(false);

  depositar(): void {

    this.cargando.set(true);

    this.cuentaService.depositar(
      this.numeroCuenta,
      { monto: this.monto }
    ).subscribe({
      next: (trx) => {

        this.cargando.set(false);

        this.snackBar.open(
          `Depósito realizado: ${trx.numeroTransaccion}`,
          'Cerrar',
          { duration: 4000 }
        );

        this.numeroCuenta = '';
        this.monto = 0;
      },
      error: (err) => {

        this.cargando.set(false);

        this.snackBar.open(
          err.error?.error ?? 'No fue posible realizar el depósito',
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }
}