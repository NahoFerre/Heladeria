export function paginar<T>(items: T[], pagina: number, tamanoPagina: number): T[] {
  const inicio = (pagina - 1) * tamanoPagina;
  return items.slice(inicio, inicio + tamanoPagina);
}

export function totalPaginas(totalItems: number, tamanoPagina: number): number {
  return Math.max(1, Math.ceil(totalItems / tamanoPagina));
}
