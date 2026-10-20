import { Producto } from './producto.model';
import { Usuario } from './usuario.model';

export interface VentaItemRequest {
  productoId: number;
  varianteId?: number;
  adicionalIds?: number[];
  cantidad: number;
}

export interface VentaRequest {
  metodoPago?: string;
  items: VentaItemRequest[];
}

export interface VentaItemAdicional {
  nombreMomento: string;
  precioAdicionalMomento: number;
}

export interface VentaItem {
  id: number;
  producto: Producto;
  cantidad: number;
  precioUnitarioMomento: number;
  costoUnitarioMomento: number;
  varianteId?: number;
  varianteNombreMomento?: string;
  promocionId?: number;
  promocionNombreMomento?: string;
  adicionales: VentaItemAdicional[];
}

export type EstadoVenta = 'CONFIRMADA' | 'ANULADA';

export interface Venta {
  id: number;
  fecha: string;
  total: number;
  metodoPago: string;
  usuario?: Usuario;
  items: VentaItem[];
  impresoEn?: string;
  estado: EstadoVenta;
  anuladaEn?: string;
  anuladaPor?: Usuario;
  motivoAnulacion?: string;
}
