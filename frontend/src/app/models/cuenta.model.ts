// Interfaces exploratorias que reflejan la jerarquia Cuenta del backend
// (com.banco.core.model.entity.Cuenta y subtipos).

export type TipoCuenta = 'Cuenta de Ahorros' | 'Cuenta Corriente' | 'Cuenta a Plazo Fijo';

export type EstadoCuenta = 'ACTIVA' | 'INACTIVA' | 'BLOQUEADA' | 'CERRADA';

export interface Cuenta {
  id: number | null;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldo: number;
  estado: EstadoCuenta;
  fechaApertura: string;
}
