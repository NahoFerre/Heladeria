import { Insumo } from './insumo.model';
import { Producto } from './producto.model';

export interface CompraItem {
  id?: number;
  insumo?: Insumo | null;
  producto?: Producto | null;
  nombreMomento: string;
  cantidad: number;
  costoUnitario: number;
  subtotal: number;
}

export interface Compra {
  id: number;
  proveedor?: string;
  numeroFactura?: string;
  metodoPago?: string;
  observaciones?: string;
  total: number;
  usuarioNombreMomento?: string;
  fecha: string;
  items: CompraItem[];
}

export interface CompraItemRequest {
  insumoId?: number;
  productoId?: number;
  cantidad: number;
  costoUnitario: number;
}

export interface CompraRequest {
  proveedor?: string;
  numeroFactura?: string;
  metodoPago?: string;
  observaciones?: string;
  items: CompraItemRequest[];
}
