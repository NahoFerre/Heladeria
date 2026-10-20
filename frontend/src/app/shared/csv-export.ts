export interface ColumnaCsv<T> {
  encabezado: string;
  valor: (fila: T) => string | number;
}

// Excel (con configuración regional en español) interpreta mejor el separador ";" que "," porque
// usa la coma como separador decimal. El BOM al inicio hace que los acentos se vean bien al abrirlo.
export function exportarCsv<T>(nombreArchivo: string, columnas: ColumnaCsv<T>[], filas: T[]): void {
  const escapar = (valor: string | number): string => {
    const texto = String(valor ?? '');
    return /[";\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
  };

  const encabezados = columnas.map((c) => escapar(c.encabezado)).join(';');
  const filasCsv = filas.map((fila) => columnas.map((c) => escapar(c.valor(fila))).join(';'));
  const contenido = [encabezados, ...filasCsv].join('\r\n');

  const bom = String.fromCharCode(0xfeff);
  const blob = new Blob([bom + contenido], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  URL.revokeObjectURL(url);
}
