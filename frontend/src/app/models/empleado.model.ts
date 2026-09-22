// Empleado, alineado a com.banco.core.model.dto.EmpleadoDTO (backend).

export type RolEmpleado = 'CAJERO' | 'GERENTE';

export interface Empleado {
  codigoEmpleado: string;
  dui: string;
  nombre: string;
  rol: RolEmpleado;
  sucursal: string;
  nivelAutorizacion: string;
}

export interface LoginRequest {
  dui: string;
  password: string;
}

// Alineado a com.banco.core.model.dto.NuevoEmpleadoRequest (backend).
// cajaAsignada solo aplica a CAJERO, montoMaximoAprobacion solo a GERENTE
// -igual que en el backend, el service no valida cual mandar, lo valida
// el constructor de Cajero/Gerente del lado del servidor-.
export interface NuevoEmpleadoRequest {
  dui: string;
  nombre: string;
  direccion: string;
  telefono: string;
  sucursal: string;
  rol: RolEmpleado;
  cajaAsignada?: string;
  montoMaximoAprobacion?: number;
  password: string;
}
