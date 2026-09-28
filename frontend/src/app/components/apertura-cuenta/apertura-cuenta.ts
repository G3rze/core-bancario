import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';

import {
  AperturaCuentaRequest,
  TipoCuentaApertura,
} from '../../models/cuenta.model';
import { CuentaService } from '../../services/cuenta.service';

@Component({
  selector: 'app-apertura-cuenta',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './apertura-cuenta.html',
  styleUrl: './apertura-cuenta.scss',
})
export class AperturaCuenta {
  private readonly cuentaService = inject(CuentaService);
  private readonly snackBar = inject(MatSnackBar);

  protected duiTitular = '';
  protected tipoCuenta: TipoCuentaApertura = 'AHORROS';
  protected saldoInicial = 0;
  protected plazoMeses: number | null = null;

  protected readonly guardando = signal(false);

  abrirCuenta(): void {
    const request: AperturaCuentaRequest = {
      duiTitular: this.duiTitular,
      tipoCuenta: this.tipoCuenta,
      saldoInicial: this.saldoInicial,
      plazoMeses:
        this.tipoCuenta === 'PLAZO_FIJO'
          ? this.plazoMeses ?? undefined
          : undefined,
    };

    this.guardando.set(true);

    this.cuentaService.abrirCuenta(request).subscribe({
      next: (cuenta) => {
        this.guardando.set(false);

        this.snackBar.open(
          `Cuenta ${cuenta.numeroCuenta} creada`,
          'Cerrar',
          { duration: 4000 }
        );

        this.duiTitular = '';
        this.saldoInicial = 0;
        this.plazoMeses = null;
        this.tipoCuenta = 'AHORROS';
      },
      error: (err) => {
        this.guardando.set(false);

        this.snackBar.open(
          err.error?.error ?? 'No se pudo abrir la cuenta',
          'Cerrar',
          { duration: 5000 }
        );
      },
    });
  }
}