import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import {
  CategoriaRanking,
  Dashboard,
  FiltroReporte,
  MovimientoInventarioReporte,
  ProductoRanking,
  ReporteGanancia,
  ResumenHoy,
  VentaPorUsuario,
  VentaReporte,
} from '../models/reporte.model';
import { Producto } from '../models/producto.model';
import { TotalPorMetodoPago } from '../models/caja.model';

@Service()
export class ReporteService {
  private http = inject(HttpClient);

  ganancias(desde?: string, hasta?: string): Observable<ReporteGanancia> {
    return this.http.get<ReporteGanancia>(`${API_URL}/reportes/ganancias`, { params: this.filtroFechas(desde, hasta) });
  }

  productosMasRentables(desde?: string, hasta?: string): Observable<ProductoRanking[]> {
    return this.http.get<ProductoRanking[]>(`${API_URL}/reportes/productos-mas-rentables`, {
      params: this.filtroFechas(desde, hasta),
    });
  }

  categoriasMasRentables(desde?: string, hasta?: string): Observable<CategoriaRanking[]> {
    return this.http.get<CategoriaRanking[]>(`${API_URL}/reportes/categorias-mas-rentables`, {
      params: this.filtroFechas(desde, hasta),
    });
  }

  stockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${API_URL}/reportes/stock-bajo`);
  }

  resumenHoy(): Observable<ResumenHoy> {
    return this.http.get<ResumenHoy>(`${API_URL}/reportes/resumen-hoy`);
  }

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${API_URL}/reportes/dashboard`);
  }

  ventasPorUsuario(desde?: string, hasta?: string): Observable<VentaPorUsuario[]> {
    return this.http.get<VentaPorUsuario[]>(`${API_URL}/reportes/ventas-por-usuario`, {
      params: this.filtroFechas(desde, hasta),
    });
  }

  ventas(filtro: FiltroReporte): Observable<VentaReporte[]> {
    return this.http.get<VentaReporte[]>(`${API_URL}/reportes/ventas`, { params: this.filtro(filtro) });
  }

  metodosPago(filtro: FiltroReporte): Observable<TotalPorMetodoPago[]> {
    return this.http.get<TotalPorMetodoPago[]>(`${API_URL}/reportes/metodos-pago`, { params: this.filtro(filtro) });
  }

  inventarioMovimientos(filtro: FiltroReporte): Observable<MovimientoInventarioReporte[]> {
    return this.http.get<MovimientoInventarioReporte[]>(`${API_URL}/reportes/inventario-movimientos`, { params: this.filtro(filtro) });
  }

  private filtroFechas(desde?: string, hasta?: string): Record<string, string> {
    const params: Record<string, string> = {};
    if (desde) params['desde'] = desde;
    if (hasta) params['hasta'] = hasta;
    return params;
  }

  private filtro(filtro: FiltroReporte): Record<string, string> {
    const params: Record<string, string> = {};
    if (filtro.desde) params['desde'] = filtro.desde;
    if (filtro.hasta) params['hasta'] = filtro.hasta;
    if (filtro.usuarioId) params['usuarioId'] = String(filtro.usuarioId);
    if (filtro.metodoPago) params['metodoPago'] = filtro.metodoPago;
    if (filtro.cajaId) params['cajaId'] = String(filtro.cajaId);
    if (filtro.productoId) params['productoId'] = String(filtro.productoId);
    if (filtro.categoria) params['categoria'] = filtro.categoria;
    return params;
  }
}
