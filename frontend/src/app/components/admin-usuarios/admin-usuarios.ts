import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AdminService } from '../../services/admin.service';

/** Activar o desactivar un usuario (cliente o empleado) por DUI. */
@Component({
  selector: 'app-admin-usuarios',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './admin-usuarios.html',
  styleUrl: './admin-usuarios.scss',
})
export class AdminUsuarios {
  private readonly admin = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);

  protected dui = '';
  protected readonly cargando = signal(false);

  cambiarEstado(activo: boolean): void {
    const dui = this.dui.trim();
    if (!dui) {
      return;
    }
    this.cargando.set(true);
    this.admin.cambiarEstado(dui, activo).subscribe({
      next: (estado) => {
        this.cargando.set(false);
        this.snackBar.open(
          `Usuario ${estado.dui} (${estado.roles.join(', ')}) ${estado.activo ? 'activado' : 'desactivado'}`,
          'Cerrar',
          { duration: 4000 },
        );
      },
      error: (err) => {
        this.cargando.set(false);
        this.snackBar.open(err.error?.error ?? 'No se pudo cambiar el estado', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
