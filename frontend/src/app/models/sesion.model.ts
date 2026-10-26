// Sesion, alineada a com.banco.core.model.dto.LoginResponse y UsuarioSesionDTO
// (backend). Solo uno de codigoEmpleado / numeroCliente viene lleno.

export type Rol = 'CLIENTE' | 'CAJERO' | 'GERENTE' | 'ADMIN';

export interface UsuarioSesion {
  dui: string;
  nombre: string;
  rol: Rol;
  codigoEmpleado: string | null;
  numeroCliente: string | null;
}

export interface Sesion {
  token: string;
  expiraEn: string;
  usuario: UsuarioSesion;
}

export interface LoginRequest {
  dui: string;
  password: string;
}

// Alineado a com.banco.core.model.dto.RegistroRequest (backend).
export interface RegistroRequest {
  dui: string;
  nombre: string;
  direccion: string;
  telefono: string;
  tipo: 'NATURAL' | 'JURIDICA';
  password: string;
}
