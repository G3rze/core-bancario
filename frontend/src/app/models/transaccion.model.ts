// Transaccion, alineada a com.banco.core.model.dto.TransaccionDTO (backend).

export type TipoTransaccion = 'Deposito' | 'Retiro' | 'Transferencia';

export type EstadoTransaccion = 'PENDIENTE' | 'COMPLETADA' | 'RECHAZADA';

export interface Transaccion {
  numeroTransaccion: string;
  tipo: TipoTransaccion;
  monto: number;
  fecha: string;
  estado: EstadoTransaccion;
  // null cuando la transaccion la hizo el cliente directo (banca en linea);
  // trae el codigo del cajero cuando vino de ventanilla.
  codigoEmpleadoCajero: string | null;
}

export interface MovimientoRequest {
  monto: number;
}

export interface TransferenciaRequest {
  numeroCuentaDestino: string;
  monto: number;
}
