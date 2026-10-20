import { MotivoMerma } from '../models/merma.model';

export const ETIQUETAS_MOTIVO_MERMA: Record<MotivoMerma, string> = {
  PRODUCTO_DERRETIDO: 'Producto derretido',
  VENCIMIENTO: 'Vencimiento',
  ROTURA: 'Rotura',
  DERRAME: 'Derrame',
  DEGUSTACION: 'Degustación',
  ERROR_PREPARACION: 'Error de preparación',
  USO_INTERNO: 'Uso interno',
  OTRO: 'Otro',
};

export function formatearMotivoMerma(motivo: MotivoMerma): string {
  return ETIQUETAS_MOTIVO_MERMA[motivo] ?? motivo;
}
