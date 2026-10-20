const NOMBRES: Record<string, string> = {
  UNIDAD: 'Unidad',
  GRAMO: 'Gramo',
  KILOGRAMO: 'Kilogramo',
  MILILITRO: 'Mililitro',
  LITRO: 'Litro',
};

export function formatearUnidadMedida(unidad: string): string {
  return NOMBRES[unidad] ?? unidad;
}
