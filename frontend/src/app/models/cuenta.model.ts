// Cuenta, alineada a com.banco.core.model.dto.CuentaDTO (backend).

export type TipoCuenta = 'Cuenta de Ahorros' | 'Cuenta Corriente' | 'Cuenta a Plazo Fijo';

export type TipoCuentaApertura = 'AHORROS' | 'CORRIENTE' | 'PLAZO_FIJO';

export type EstadoCuenta = 'ACTIVA' | 'INACTIVA' | 'BLOQUEADA' | 'CERRADA';

export interface Cuenta {
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldo: number;
  estado: EstadoCuenta;
  fechaApertura: string;
  numeroClienteTitular: string;
  // Solo la respuesta de apertura lo trae.
  comprobante: string | null;
}

export interface AperturaCuentaRequest {
  // Un CLIENTE lo omite: el backend usa el DUI del token.
  duiTitular?: string;
  tipoCuenta: TipoCuentaApertura;
  saldoInicial: number;
  // Solo aplica (y es obligatorio) cuando tipoCuenta es PLAZO_FIJO, igual
  // que en el backend (com.banco.core.model.dto.AperturaCuentaRequest).
  plazoMeses?: number;
}
