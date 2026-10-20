import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Compra, CompraRequest } from '../models/compra.model';
import { FiltroReporte } from '../models/reporte.model';

@Service()
export class CompraService {
  private http = inject(HttpClient);

  listar(filtro?: FiltroReporte): Observable<Compra[]> {
    return this.http.get<Compra[]>(`${API_URL}/compras`, { params: this.filtro(filtro) });
  }

  obtener(id: number): Observable<Compra> {
    return this.http.get<Compra>(`${API_URL}/compras/${id}`);
  }

  crear(request: CompraRequest): Observable<Compra> {
    return this.http.post<Compra>(`${API_URL}/compras`, request);
  }

  private filtro(filtro?: FiltroReporte): Record<string, string> {
    const params: Record<string, string> = {};
    if (!filtro) return params;
    if (filtro.desde) params['desde'] = filtro.desde;
    if (filtro.hasta) params['hasta'] = filtro.hasta;
    if (filtro.usuarioId) params['usuarioId'] = String(filtro.usuarioId);
    if (filtro.metodoPago) params['metodoPago'] = filtro.metodoPago;
    return params;
  }
}
