const NOMBRES: Record<string, string> = {
  EFECTIVO: 'Efectivo',
  TARJETA: 'Tarjeta',
  TRANSFERENCIA: 'Transferencia',
  QR: 'QR',
};

export function formatearMetodoPago(metodo: string): string {
  return NOMBRES[metodo] ?? metodo;
}
