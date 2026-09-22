import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { NuevoEmpleadoRequest, RolEmpleado } from '../../models/empleado.model';
import { EmpleadoService } from '../../services/empleado.service';

@Component({
  selector: 'app-crear-empleado',
  imports: [
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './crear-empleado.html',
  styleUrl: './crear-empleado.scss',
})
export class CrearEmpleado {
  private readonly empleadoService = inject(EmpleadoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  protected dui = '';
  protected nombre = '';
  protected direccion = '';
  protected telefono = '';
  protected sucursal = '';
  protected rol: RolEmpleado = 'CAJERO';
  protected cajaAsignada = '';
  protected montoMaximoAprobacion: number | null = null;
  protected password = '';

  protected readonly guardando = signal(false);

  registrar(): void {
    const request: NuevoEmpleadoRequest = {
      dui: this.dui,
      nombre: this.nombre,
      direccion: this.direccion,
      telefono: this.telefono,
      sucursal: this.sucursal,
      rol: this.rol,
      password: this.password,
      cajaAsignada: this.rol === 'CAJERO' ? this.cajaAsignada : undefined,
      montoMaximoAprobacion:
        this.rol === 'GERENTE' && this.montoMaximoAprobacion !== null ? this.montoMaximoAprobacion : undefined,
    };

    this.guardando.set(true);
    this.empleadoService.registrar(request).subscribe({
      next: (empleado) => {
        this.guardando.set(false);
        this.snackBar.open(`Empleado ${empleado.codigoEmpleado} creado`, 'Cerrar', { duration: 4000 });
        this.router.navigateByUrl('/ventanilla');
      },
      error: (err) => {
        this.guardando.set(false);
        this.snackBar.open(err.error?.error ?? 'No se pudo crear el empleado', 'Cerrar', { duration: 5000 });
      },
    });
  }
}
