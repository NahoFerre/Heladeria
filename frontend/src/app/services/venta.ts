import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Venta, VentaRequest } from '../models/venta.model';

@Service()
export class VentaService {
  private http = inject(HttpClient);

  listar(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${API_URL}/ventas`);
  }

  obtener(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${API_URL}/ventas/${id}`);
  }

  crear(venta: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(`${API_URL}/ventas`, venta);
  }

  marcarImpresa(id: number): Observable<Venta> {
    return this.http.put<Venta>(`${API_URL}/ventas/${id}/marcar-impresa`, {});
  }

  anular(id: number, motivo: string): Observable<Venta> {
    return this.http.put<Venta>(`${API_URL}/ventas/${id}/anular`, { motivo });
  }
}
