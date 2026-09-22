import { Component } from '@angular/core';
import { Cliente } from '../../models/usuario.model';

// Datos de ejemplo mientras no existe la API real. Se reemplazaran por un
// ClienteService que consuma el backend una vez exista el Controller/DTO.
const CLIENTES_MOCK: Cliente[] = [
  {
    dui: '12345678-9',
    nombre: 'Ana Lopez',
    direccion: 'San Salvador',
    telefono: '7777-7777',
    activo: true,
    numeroCliente: 'CLI-000001',
    tipo: 'NATURAL',
  },
  {
    dui: '98765432-1',
    nombre: 'Comercial El Roble S.A. de C.V.',
    direccion: 'Santa Ana',
    telefono: '2222-3333',
    activo: true,
    numeroCliente: 'CLI-000002',
    tipo: 'JURIDICA',
  },
];

@Component({
  selector: 'app-cliente-lista',
  imports: [],
  templateUrl: './cliente-lista.html',
  styleUrl: './cliente-lista.scss',
})
export class ClienteLista {
  protected readonly clientes = CLIENTES_MOCK;
}
