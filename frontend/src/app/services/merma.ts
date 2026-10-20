import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Merma, MermaRequest } from '../models/merma.model';
import { FiltroReporte } from '../models/reporte.model';

@Service()
export class MermaService {
  private http = inject(HttpClient);

  listar(filtro?: FiltroReporte): Observable<Merma[]> {
    return this.http.get<Merma[]>(`${API_URL}/mermas`, { params: this.filtro(filtro) });
  }

  crear(request: MermaRequest): Observable<Merma> {
    return this.http.post<Merma>(`${API_URL}/mermas`, request);
  }

  private filtro(filtro?: FiltroReporte): Record<string, string> {
    const params: Record<string, string> = {};
    if (!filtro) return params;
    if (filtro.desde) params['desde'] = filtro.desde;
    if (filtro.hasta) params['hasta'] = filtro.hasta;
    if (filtro.usuarioId) params['usuarioId'] = String(filtro.usuarioId);
    if (filtro.productoId) params['productoId'] = String(filtro.productoId);
    return params;
  }
}
