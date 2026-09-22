import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected dui = '';
  protected password = '';
  protected readonly error = signal<string | null>(null);
  protected readonly cargando = signal(false);

  ingresar(): void {
    this.error.set(null);
    this.cargando.set(true);

    this.auth.login({ dui: this.dui, password: this.password }).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigateByUrl('/ventanilla');
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.error ?? 'No se pudo iniciar sesion');
      },
    });
  }
}
