import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { NuevoClienteRequest, TipoCliente } from '../../models/usuario.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-crear-cliente',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './crear-cliente.html',
  styleUrl: './crear-cliente.scss',
})
export class CrearCliente {
  private readonly clienteService = inject(ClienteService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  protected dui = '';
  protected nombre = '';
  protected direccion = '';
  protected telefono = '';
  protected tipo: TipoCliente = 'NATURAL';

  protected readonly guardando = signal(false);

  registrar(): void {
    const request: NuevoClienteRequest = {
      dui: this.dui,
      nombre: this.nombre,
      direccion: this.direccion,
      telefono: this.telefono,
      tipo: this.tipo,
    };

    this.guardando.set(true);
    this.clienteService.registrar(request).subscribe({
      next: (cliente) => {
        this.guardando.set(false);
        this.snackBar.open(`Cliente ${cliente.numeroCliente} creado`, 'Cerrar', { duration: 4000 });
        this.router.navigateByUrl('/clientes');
      },
      error: (err) => {
        this.guardando.set(false);
        this.snackBar.open(err.error?.error ?? 'No se pudo crear el cliente', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
