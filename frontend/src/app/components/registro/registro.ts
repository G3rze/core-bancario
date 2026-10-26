import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';

import { RegistroRequest } from '../../models/sesion.model';
import { TipoCliente } from '../../models/usuario.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-registro',
  imports: [
    FormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  protected dui = '';
  protected nombre = '';
  protected direccion = '';
  protected telefono = '';
  protected tipo: TipoCliente = 'NATURAL';
  protected password = '';

  protected readonly guardando = signal(false);

  registrar(): void {
    const request: RegistroRequest = {
      dui: this.dui,
      nombre: this.nombre,
      direccion: this.direccion,
      telefono: this.telefono,
      tipo: this.tipo,
      password: this.password,
    };

    this.guardando.set(true);
    this.auth.registrar(request).subscribe({
      next: (cliente) => {
        this.guardando.set(false);
        this.snackBar.open(`Registro exitoso. Tu numero de cliente es ${cliente.numeroCliente}`, 'Cerrar', {
          duration: 6000,
        });
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.guardando.set(false);
        this.snackBar.open(err.error?.error ?? 'No se pudo completar el registro', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
