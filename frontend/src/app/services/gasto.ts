import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Gasto, GastoRequest } from '../models/gasto.model';
import { FiltroReporte } from '../models/reporte.model';

@Service()
export class GastoService {
  private http = inject(HttpClient);

  listar(filtro?: FiltroReporte): Observable<Gasto[]> {
    return this.http.get<Gasto[]>(`${API_URL}/gastos`, { params: this.filtro(filtro) });
  }

  crear(request: GastoRequest): Observable<Gasto> {
    return this.http.post<Gasto>(`${API_URL}/gastos`, request);
  }

  private filtro(filtro?: FiltroReporte): Record<string, string> {
    const params: Record<string, string> = {};
    if (!filtro) return params;
    if (filtro.desde) params['desde'] = filtro.desde;
    if (filtro.hasta) params['hasta'] = filtro.hasta;
    if (filtro.usuarioId) params['usuarioId'] = String(filtro.usuarioId);
    if (filtro.metodoPago) params['metodoPago'] = filtro.metodoPago;
    if (filtro.categoria) params['categoria'] = filtro.categoria;
    return params;
  }
}
