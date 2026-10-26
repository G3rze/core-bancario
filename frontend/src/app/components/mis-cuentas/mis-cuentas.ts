import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';

import { AperturaCuentaRequest, Cuenta, TipoCuentaApertura } from '../../models/cuenta.model';
import { CuentaService } from '../../services/cuenta.service';

@Component({
  selector: 'app-mis-cuentas',
  imports: [
    FormsModule,
    DecimalPipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './mis-cuentas.html',
  styleUrl: './mis-cuentas.scss',
})
export class MisCuentas implements OnInit {
  private readonly cuentaService = inject(CuentaService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly cuentas = signal<Cuenta[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly abriendo = signal(false);
  protected readonly guardando = signal(false);
  protected readonly comprobante = signal<string | null>(null);

  protected tipoCuenta: TipoCuentaApertura = 'AHORROS';
  protected saldoInicial: number | null = null;
  protected plazoMeses: number | null = null;

  ngOnInit(): void {
    this.cargar();
  }

  abrirCuenta(): void {
    if (this.saldoInicial === null) {
      return;
    }
    // Sin duiTitular: para un CLIENTE el backend usa el DUI del token.
    const request: AperturaCuentaRequest = {
      tipoCuenta: this.tipoCuenta,
      saldoInicial: this.saldoInicial,
      plazoMeses: this.tipoCuenta === 'PLAZO_FIJO' ? (this.plazoMeses ?? undefined) : undefined,
    };

    this.guardando.set(true);
    this.cuentaService.abrirCuenta(request).subscribe({
      next: (cuenta) => {
        this.guardando.set(false);
        this.abriendo.set(false);
        this.comprobante.set(cuenta.comprobante);
        this.saldoInicial = null;
        this.plazoMeses = null;
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.snackBar.open(err.error?.error ?? 'No se pudo abrir la cuenta', 'Cerrar', { duration: 5000 });
      },
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.cuentaService.misCuentas().subscribe({
      next: (cuentas) => {
        this.cuentas.set(cuentas);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.error ?? 'No fue posible cargar tus cuentas');
      },
    });
  }
}
