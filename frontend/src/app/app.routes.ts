import { Routes } from '@angular/router';
import { ClienteLista } from './components/cliente-lista/cliente-lista';

export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
  { path: 'clientes', component: ClienteLista },
];
