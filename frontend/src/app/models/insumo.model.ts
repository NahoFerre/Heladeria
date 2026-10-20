export type UnidadMedida = 'UNIDAD' | 'GRAMO' | 'KILOGRAMO' | 'MILILITRO' | 'LITRO';

export interface Insumo {
  id?: number;
  nombre: string;
  unidadMedida: UnidadMedida;
  costoUnitario: number;
  stockActual: number;
  stockMinimo: number;
  proveedor?: string;
  activo: boolean;
}
