import { Insumo } from './insumo.model';

export interface RecetaItem {
  id?: number;
  insumo: Insumo;
  cantidad: number;
}

export interface ProductoVariante {
  id?: number;
  nombre: string;
  precio: number;
  costo: number;
  sku?: string;
  stockActual: number;
  stockMinimo: number;
  activo: boolean;
  receta?: RecetaItem[];
}

export interface Adicional {
  id?: number;
  nombre: string;
  precioAdicional: number;
  costoAdicional: number;
  controlaStock: boolean;
  stockActual: number;
  stockMinimo: number;
  activo: boolean;
}

export interface GrupoAdicionales {
  id?: number;
  nombre: string;
  minSeleccion: number;
  maxSeleccion: number;
  opciones: Adicional[];
}

export interface Producto {
  id?: number;
  nombre: string;
  categoria?: string;
  costoUnitario: number;
  precioVenta: number;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
  activo: boolean;
  variantes?: ProductoVariante[];
  gruposAdicionales?: GrupoAdicionales[];
  receta?: RecetaItem[];
}

export type TipoMovimiento = 'ENTRADA' | 'SALIDA' | 'AJUSTE';

export interface MovimientoInventario {
  id?: number;
  producto?: Producto;
  tipo: TipoMovimiento;
  cantidad: number;
  motivo?: string;
  fecha?: string;
}

export interface ConfigurarOpcionesRequest {
  variantes: ProductoVariante[];
  gruposAdicionales: GrupoAdicionales[];
}

export interface ItemRecetaRequest {
  insumoId: number;
  cantidad: number;
}

export interface RecetaVarianteRequest {
  varianteId: number;
  items: ItemRecetaRequest[];
}

export interface ConfigurarRecetaRequest {
  recetaProducto: ItemRecetaRequest[];
  recetasVariantes: RecetaVarianteRequest[];
}
