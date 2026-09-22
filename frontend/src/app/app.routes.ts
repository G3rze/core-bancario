import { Routes } from '@angular/router';
import { ClienteLista } from './components/cliente-lista/cliente-lista';
import { Login } from './components/login/login';
import { Ventanilla } from './components/ventanilla/ventanilla';
import { rolGuard } from './guards/rol.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
  { path: 'clientes', component: ClienteLista },
  { path: 'login', component: Login },
  { path: 'ventanilla', component: Ventanilla, canActivate: [rolGuard(['CAJERO', 'GERENTE'])] },
];
