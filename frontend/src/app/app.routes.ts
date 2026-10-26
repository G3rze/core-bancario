import { Routes } from '@angular/router';

import { AdminUsuarios } from './components/admin-usuarios/admin-usuarios';
import { ClienteLista } from './components/cliente-lista/cliente-lista';
import { CrearCliente } from './components/crear-cliente/crear-cliente';
import { CrearEmpleado } from './components/crear-empleado/crear-empleado';
import { CuentaDetalle } from './components/cuenta-detalle/cuenta-detalle';
import { Login } from './components/login/login';
import { MisCuentas } from './components/mis-cuentas/mis-cuentas';
import { Registro } from './components/registro/registro';
import { Ventanilla } from './components/ventanilla/ventanilla';
import { rolGuard } from './guards/rol.guard';
import { inicioGuard } from './guards/inicio.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', canActivate: [inicioGuard], children: [] },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },

  // Cliente
  { path: 'mis-cuentas', component: MisCuentas, canActivate: [rolGuard(['CLIENTE'])] },
  {
    path: 'cuentas/:numero',
    component: CuentaDetalle,
    canActivate: [rolGuard(['CLIENTE', 'CAJERO', 'GERENTE'])],
  },

  // Cajero / Gerente
  { path: 'ventanilla', component: Ventanilla, canActivate: [rolGuard(['CAJERO', 'GERENTE'])] },
  { path: 'clientes', component: ClienteLista, canActivate: [rolGuard(['CAJERO', 'GERENTE'])] },
  { path: 'clientes/nuevo', component: CrearCliente, canActivate: [rolGuard(['CAJERO', 'GERENTE'])] },

  // Gerente
  { path: 'empleados/nuevo', component: CrearEmpleado, canActivate: [rolGuard(['GERENTE'])] },

  // Admin
  { path: 'admin/usuarios', component: AdminUsuarios, canActivate: [rolGuard(['ADMIN'])] },

  { path: '**', redirectTo: '' },
];
