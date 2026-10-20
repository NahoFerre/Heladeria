import { Producto } from '../models/producto.model';
import { Promocion } from '../models/promocion.model';

// Espeja exactamente PromocionService (backend) — mejorPromocionPara/precioConPromocion/aplicaA — para que
// el carrito de Venta muestre el mismo precio que después va a cobrar el servidor al confirmar.
// Las promociones que recibe ya vienen filtradas por vigencia de fecha (GET /promociones/vigentes),
// así que acá no hace falta repetir esa comparación de fechas.
export function mejorPromocionPara(producto: Producto, cantidad: number, precioBase: number, promociones: Promocion[]): Promocion | null {
  const candidatas = promociones.filter((p) => aplicaA(p, producto) && cantidad >= p.cantidadMinima);
  if (candidatas.length === 0) return null;
  return candidatas.reduce((mejor, actual) =>
    precioConPromocion(actual, precioBase) < precioConPromocion(mejor, precioBase) ? actual : mejor,
  );
}

export function precioConPromocion(promocion: Promocion, precioBase: number): number {
  let resultado: number;
  switch (promocion.tipo) {
    case 'PORCENTAJE':
      resultado = (precioBase * (100 - promocion.valor)) / 100;
      break;
    case 'MONTO_FIJO':
      resultado = precioBase - promocion.valor;
      break;
    case 'PRECIO_ESPECIAL':
      resultado = promocion.valor;
      break;
  }
  return Math.max(0, resultado);
}

function aplicaA(promocion: Promocion, producto: Producto): boolean {
  if (promocion.producto) return promocion.producto.id === producto.id;
  if (promocion.categoria) return promocion.categoria === producto.categoria;
  return true;
}
