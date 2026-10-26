// Transaccion, alineada a com.banco.core.model.dto.TransaccionDTO (backend).

export type TipoTransaccion = 'Deposito' | 'Retiro' | 'Transferencia';

// Valores que acepta ?tipo= en el historial (nombre del enum del backend).
export type TipoTransaccionFiltro = 'DEPOSITO' | 'RETIRO' | 'TRANSFERENCIA';

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
  // null en movimientos anteriores a que el backend guardara estos datos.
  saldoPosterior: number | null;
  comprobante: string | null;
  // Solo lo llenan las respuestas de ventanilla.
  selloDigital: string | null;
}

export interface HistorialResumen {
  ingresos: number;
  egresos: number;
}

export interface MovimientoRequest {
  monto: number;
}

export interface TransferenciaRequest {
  numeroCuentaDestino: string;
  monto: number;
}
