// Cliente, alineado a com.banco.core.model.dto.ClienteDTO (backend). No hay
// campo `rol` ni `id`: ClienteDTO no los expone (rol solo existe en
// EmpleadoDTO, ver empleado.model.ts).

export type TipoCliente = 'NATURAL' | 'JURIDICA';

export interface Cliente {
  numeroCliente: string;
  dui: string;
  nombre: string;
  direccion: string;
  telefono: string;
  tipo: TipoCliente;
  activo: boolean;
}

export interface NuevoClienteRequest {
  dui: string;
  nombre: string;
  direccion: string;
  telefono: string;
  tipo: TipoCliente;
}
