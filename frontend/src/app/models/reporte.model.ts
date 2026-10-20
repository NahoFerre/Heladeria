import { CajaActual } from './caja.model';

export interface ReporteGanancia {
  totalVentas: number;
  totalCosto: number;
  ganancia: number;
  cantidadItemsVendidos: number;
  cantidadVentas: number;
  costoMerma: number;
  gastosOperativos: number;
  gananciaOperativa: number;
  margenPorcentaje: number;
  ticketPromedio: number;
}

export interface ProductoRanking {
  productoId: number;
  nombre: string;
  cantidadVendida: number;
  ingresos: number;
  ganancia: number;
}

export interface CategoriaRanking {
  categoria: string;
  cantidadVendida: number;
  ingresos: number;
  ganancia: number;
}

export interface VentaPorUsuario {
  usuario: string;
  cantidadVentas: number;
  totalVendido: number;
}

export interface ResumenHoy {
  ventasHoy: number;
  totalVendidoHoy: number;
  productosStockBajo: number;
}

export interface VentasPeriodo {
  cantidadVentas: number;
  totalVendido: number;
  variacionPorcentaje: number | null;
}

export interface DashboardVentas {
  hoy: VentasPeriodo;
  semana: VentasPeriodo;
  mes: VentasPeriodo;
}

export interface DashboardProductos {
  masVendido: ProductoRanking | null;
  mayorGanancia: ProductoRanking | null;
  menorRotacion: ProductoRanking | null;
  mayorMargen: ProductoRanking | null;
}

export interface StockBajoItem {
  id: number;
  nombre: string;
  stockActual: number;
  stockMinimo: number;
  unidadMedida: string;
}

export interface ProductoSinMovimiento {
  id: number;
  nombre: string;
  categoria: string | null;
}

export interface DashboardInventario {
  stockBajoProductos: StockBajoItem[];
  stockBajoInsumos: StockBajoItem[];
  productosSinMovimiento: ProductoSinMovimiento[];
}

export interface Dashboard {
  ventas: DashboardVentas;
  productos: DashboardProductos;
  cajaActual: CajaActual | null;
  inventario: DashboardInventario;
}

export interface VentaReporte {
  id: number;
  fecha: string;
  usuario: string;
  metodoPago: string;
  total: number;
  cajaId: number;
}

export interface MovimientoInventarioReporte {
  id: number;
  fecha: string;
  producto: string;
  tipo: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  cantidad: number;
  motivo: string | null;
}

export interface FiltroReporte {
  desde?: string;
  hasta?: string;
  usuarioId?: number;
  metodoPago?: string;
  cajaId?: number;
  productoId?: number;
  categoria?: string;
}
