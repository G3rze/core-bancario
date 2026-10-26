// Alineado a com.banco.core.model.dto.ReporteFinancieroDTO (backend).
export interface ReporteFinanciero {
  totalClientes: number;
  totalCuentas: number;
  cuentasPorTipo: Record<string, number>;
  saldoTotal: number;
  totalTransacciones: number;
  transaccionesRechazadas: number;
  montoPorTipoTransaccion: Record<string, number>;
}
