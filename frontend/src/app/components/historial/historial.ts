import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { Transaccion } from '../../models/transaccion.model';
import { CuentaService } from '../../services/cuenta.service';

@Component({
  selector: 'app-historial',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './historial.html',
  styleUrl: './historial.scss',
})
export class Historial {

  private readonly cuentaService = inject(CuentaService);

  protected numeroCuenta = '';

  protected tipoFiltro = '';

  protected transacciones: Transaccion[] = [];

  buscar(): void {

    this.cuentaService.historial(this.numeroCuenta)
      .subscribe({
        next: (transacciones) => {

          if (!this.tipoFiltro) {
            this.transacciones = transacciones;
            return;
          }

          this.transacciones = transacciones.filter(
            trx => trx.tipo === this.tipoFiltro
          );
        },
        error: () => {
          this.transacciones = [];
        }
      });
  }
}