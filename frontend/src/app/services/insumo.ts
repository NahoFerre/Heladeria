import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Insumo } from '../models/insumo.model';

@Service()
export class InsumoService {
  private http = inject(HttpClient);

  listar(soloActivos = true): Observable<Insumo[]> {
    return this.http.get<Insumo[]>(`${API_URL}/insumos`, { params: { soloActivos } });
  }

  obtener(id: number): Observable<Insumo> {
    return this.http.get<Insumo>(`${API_URL}/insumos/${id}`);
  }

  crear(insumo: Insumo): Observable<Insumo> {
    return this.http.post<Insumo>(`${API_URL}/insumos`, insumo);
  }

  actualizar(id: number, insumo: Insumo): Observable<Insumo> {
    return this.http.put<Insumo>(`${API_URL}/insumos/${id}`, insumo);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/insumos/${id}`);
  }
}
