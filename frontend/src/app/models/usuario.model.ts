// Interfaces exploratorias que reflejan el dominio planeado en el backend
// (com.banco.core.model.entity). Se ajustaran cuando existan los DTOs reales
// expuestos por la API REST.

export type RolUsuario = 'CLIENTE' | 'CAJERO' | 'GERENTE';

export type TipoCliente = 'NATURAL' | 'JURIDICA';

export interface Usuario {
  id: number | null;
  dui: string;
  nombre: string;
  direccion: string;
  telefono: string;
  rol: RolUsuario;
  activo: boolean;
}

export interface Cliente extends Usuario {
  rol: 'CLIENTE';
  numeroCliente: string;
  tipo: TipoCliente;
}
