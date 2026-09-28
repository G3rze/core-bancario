import { Component, OnInit, inject } from '@angular/core';
import { Cliente } from '../../models/usuario.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-cliente-lista',
  standalone: true,
  imports: [],
  templateUrl: './cliente-lista.html',
  styleUrl: './cliente-lista.scss',
})
export class ClienteLista implements OnInit {

  private readonly clienteService = inject(ClienteService);

  protected clientes: Cliente[] = [];
  protected cargando = false;
  protected error = '';

  ngOnInit(): void {
    this.cargarClientes();
  }

  private cargarClientes(): void {

    this.cargando = true;
    this.error = '';

    this.clienteService.listarTodos().subscribe({
      next: (clientes) => {
        this.clientes = clientes;
        this.cargando = false;
      },
      error: () => {
        this.error = 'No fue posible cargar los clientes';
        this.cargando = false;
      },
    });
  }
}
``
