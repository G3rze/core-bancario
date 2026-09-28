import { Routes } from '@angular/router';
import { ClienteLista } from './components/cliente-lista/cliente-lista';
import { CrearCliente } from './components/crear-cliente/crear-cliente';
import { CrearEmpleado } from './components/crear-empleado/crear-empleado';
import { Login } from './components/login/login';
import { Ventanilla } from './components/ventanilla/ventanilla';
import { rolGuard } from './guards/rol.guard';
import { AperturaCuenta } from './components/apertura-cuenta/apertura-cuenta';
import { Deposito } from './components/deposito/deposito';
import { Retiro } from './components/retiro/retiro';
import { Transferencia } from './components/transferencia/transferencia';

export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
  { path: 'clientes', component: ClienteLista },
  { path: 'clientes/nuevo', component: CrearCliente },
  { path: 'login', component: Login },
  { path: 'ventanilla', component: Ventanilla, canActivate: [rolGuard(['CAJERO', 'GERENTE'])] },
  { path: 'empleados/nuevo', component: CrearEmpleado, canActivate: [rolGuard(['GERENTE'])] },
  { path: 'cuentas/nueva', component: AperturaCuenta },
  { path: 'cuentas/deposito', component: Deposito },
  { path: 'cuentas/retiro', component: Retiro },
  { path: 'cuentas/transferencia', component: Transferencia },
];
