import { Insumo } from './insumo.model';
import { Producto } from './producto.model';

export type MotivoMerma =
  | 'PRODUCTO_DERRETIDO'
  | 'VENCIMIENTO'
  | 'ROTURA'
  | 'DERRAME'
  | 'DEGUSTACION'
  | 'ERROR_PREPARACION'
  | 'USO_INTERNO'
  | 'OTRO';

export interface Merma {
  id: number;
  insumo?: Insumo | null;
  producto?: Producto | null;
  nombreMomento: string;
  cantidad: number;
  costoUnitario: number;
  costoTotal: number;
  motivo: MotivoMerma;
  observacion?: string;
  usuarioNombreMomento?: string;
  fecha: string;
}

export interface MermaRequest {
  insumoId?: number;
  productoId?: number;
  cantidad: number;
  motivo: MotivoMerma;
  observacion?: string;
}
