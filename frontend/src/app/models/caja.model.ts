export interface TotalPorMetodoPago {
  metodoPago: string;
  cantidadVentas: number;
  totalVendido: number;
}

export interface CajaActual {
  id: number;
  abiertaEn: string;
  montoApertura: number;
  usuarioApertura?: string;
  totalEfectivoVendido: number;
  totalesPorMetodoPago: TotalPorMetodoPago[];
  totalIngresos: number;
  totalRetiros: number;
  totalGastos: number;
  efectivoEsperadoParcial: number;
}

export interface CierreCaja {
  id: number;
  abiertaEn: string;
  cerradaEn: string;
  montoApertura: number;
  montoCierreEsperado: number;
  montoCierreContado: number;
  diferencia: number;
  totalVentas: number;
  usuarioApertura?: string;
  usuarioCierre?: string;
  observacion?: string;
  totalesPorMetodoPago: TotalPorMetodoPago[];
  totalIngresos: number;
  totalRetiros: number;
  totalGastos: number;
}

export interface CajaHistorial {
  id: number;
  abiertaEn: string;
  cerradaEn: string;
  montoApertura: number;
  montoCierreEsperado: number;
  montoCierreContado: number;
  diferencia: number;
  totalVentas: number;
  usuarioApertura?: string;
  usuarioCierre?: string;
  totalIngresos: number;
  totalRetiros: number;
  totalGastos: number;
}

export interface AbrirCajaRequest {
  montoInicial: number;
}

export interface CerrarCajaRequest {
  montoContado: number;
  observacion?: string;
}

export type TipoMovimientoCaja = 'RETIRO' | 'INGRESO' | 'GASTO';

export interface MovimientoCaja {
  id: number;
  tipo: TipoMovimientoCaja;
  monto: number;
  motivo: string;
  usuario?: string;
  fecha: string;
}

export interface MovimientoCajaRequest {
  tipo: TipoMovimientoCaja;
  monto: number;
  motivo: string;
}
