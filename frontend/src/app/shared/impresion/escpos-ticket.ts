import { Venta, VentaItem } from '../../models/venta.model';
import { formatearMetodoPago } from '../metodo-pago';
import { LOGO_HELADERIA_ESCPOS } from './logo-heladeria-escpos';

// Ancho en columnas de la GL-33 (58mm, fuente A a 12cpp): estándar en impresoras ESC/POS de 58mm.
// Si el ticket sale con márgenes raros en otra impresora térmica, es lo primero a ajustar.
const ANCHO_COLUMNAS = 32;

const ESC = '\x1B';
const GS = '\x1D';

const INIT = ESC + '@';
const ALINEAR_IZQUIERDA = ESC + 'a' + '\x00';
const ALINEAR_CENTRO = ESC + 'a' + '\x01';
const NEGRITA_ON = ESC + 'E' + '\x01';
const NEGRITA_OFF = ESC + 'E' + '\x00';
const TAMANO_DOBLE = GS + '!' + '\x11';
const TAMANO_NORMAL = GS + '!' + '\x00';
const CORTE_PAPEL = GS + 'V' + '\x00';

function formatearGuaranies(valor: number): string {
  return 'Gs. ' + new Intl.NumberFormat('es-PY', { maximumFractionDigits: 0 }).format(valor);
}

function envolverTexto(texto: string, ancho: number): string[] {
  const palabras = texto.split(' ');
  const lineas: string[] = [];
  let actual = '';

  for (const palabra of palabras) {
    const candidata = actual ? `${actual} ${palabra}` : palabra;
    if (candidata.length > ancho && actual) {
      lineas.push(actual);
      actual = palabra;
    } else {
      actual = candidata;
    }
  }
  if (actual) lineas.push(actual);
  return lineas.length > 0 ? lineas : [''];
}

function lineaDosColumnas(izquierda: string, derecha: string, ancho: number): string {
  const espacio = ancho - izquierda.length - derecha.length;
  if (espacio < 1) {
    return `${izquierda.slice(0, ancho - derecha.length - 1)} ${derecha}`;
  }
  return izquierda + ' '.repeat(espacio) + derecha;
}

function lineaSeparadora(): string {
  return '-'.repeat(ANCHO_COLUMNAS);
}

function precioUnitario(item: VentaItem): number {
  return (
    item.precioUnitarioMomento +
    item.adicionales.reduce((acc, a) => acc + a.precioAdicionalMomento, 0)
  );
}

function subtotal(item: VentaItem): number {
  return item.cantidad * precioUnitario(item);
}

function lineasItem(item: VentaItem): string[] {
  const nombre = item.varianteNombreMomento
    ? `${item.producto.nombre} - ${item.varianteNombreMomento}`
    : item.producto.nombre;
  const lineas = envolverTexto(nombre, ANCHO_COLUMNAS);

  lineas.push(
    lineaDosColumnas(
      `${item.cantidad} x ${formatearGuaranies(precioUnitario(item))}`,
      formatearGuaranies(subtotal(item)),
      ANCHO_COLUMNAS,
    ),
  );

  if (item.adicionales.length > 0) {
    const nombresAdicionales = item.adicionales.map((a) => a.nombreMomento).join(', ');
    lineas.push(...envolverTexto(`+ ${nombresAdicionales}`, ANCHO_COLUMNAS));
  }
  if (item.promocionNombreMomento) {
    lineas.push(...envolverTexto(`Promo: ${item.promocionNombreMomento}`, ANCHO_COLUMNAS));
  }

  return lineas;
}

// Arma el flujo de comandos ESC/POS crudos para el ticket, pensado para una impresora térmica
// de 58mm (GL-33 y clones equivalentes). Se manda tal cual a QZ Tray, que lo escribe byte a byte
// en la impresora sin pasar por el diálogo de impresión del navegador.
export function construirTicketEscPos(venta: Venta): string[] {
  const lineas: string[] = [];

  lineas.push(INIT);
  lineas.push(ALINEAR_CENTRO);
  lineas.push(LOGO_HELADERIA_ESCPOS + '\n');
  lineas.push(NEGRITA_ON + 'Heladeria & Acai' + NEGRITA_OFF + '\n');
  lineas.push(`Ticket #${venta.id}\n`);
  lineas.push(
    new Intl.DateTimeFormat('es-PY', { dateStyle: 'short', timeStyle: 'short' }).format(
      new Date(venta.fecha),
    ) + '\n',
  );
  lineas.push(lineaSeparadora() + '\n');

  lineas.push(ALINEAR_IZQUIERDA);
  for (const item of venta.items) {
    lineas.push(lineasItem(item).join('\n') + '\n');
  }
  lineas.push(lineaSeparadora() + '\n');

  lineas.push(TAMANO_DOBLE);
  lineas.push(
    NEGRITA_ON +
      lineaDosColumnas('TOTAL', formatearGuaranies(venta.total), ANCHO_COLUMNAS / 2) +
      NEGRITA_OFF +
      '\n',
  );
  lineas.push(TAMANO_NORMAL);

  lineas.push(`Metodo de pago: ${formatearMetodoPago(venta.metodoPago)}\n`);
  if (venta.usuario) {
    lineas.push(`Vendedor: ${venta.usuario.username}\n`);
  }

  lineas.push(ALINEAR_CENTRO);
  lineas.push('\nGracias por su compra!\n');
  lineas.push('\n\n\n');
  lineas.push(CORTE_PAPEL);

  return lineas;
}
