import { Producto } from './producto.model';

export type TipoPromocion = 'PORCENTAJE' | 'MONTO_FIJO' | 'PRECIO_ESPECIAL';

export interface Promocion {
  id: number;
  nombre: string;
  tipo: TipoPromocion;
  valor: number;
  producto?: Producto | null;
  categoria?: string | null;
  fechaInicio?: string | null;
  fechaFin?: string | null;
  cantidadMinima: number;
  activo: boolean;
  creadoEn: string;
}

export interface PromocionRequest {
  nombre: string;
  tipo: TipoPromocion;
  valor: number;
  productoId?: number | null;
  categoria?: string | null;
  fechaInicio?: string | null;
  fechaFin?: string | null;
  cantidadMinima: number;
  activo: boolean;
}
