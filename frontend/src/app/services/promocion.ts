import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Promocion, PromocionRequest } from '../models/promocion.model';

@Service()
export class PromocionService {
  private http = inject(HttpClient);

  listar(soloActivas = false): Observable<Promocion[]> {
    return this.http.get<Promocion[]>(`${API_URL}/promociones`, { params: { soloActivas } });
  }

  vigentes(): Observable<Promocion[]> {
    return this.http.get<Promocion[]>(`${API_URL}/promociones/vigentes`);
  }

  crear(request: PromocionRequest): Observable<Promocion> {
    return this.http.post<Promocion>(`${API_URL}/promociones`, request);
  }

  actualizar(id: number, request: PromocionRequest): Observable<Promocion> {
    return this.http.put<Promocion>(`${API_URL}/promociones/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/promociones/${id}`);
  }
}
