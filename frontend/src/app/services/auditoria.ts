import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { PaginaBackend, RegistroAuditoria } from '../models/auditoria.model';

export interface FiltroAuditoria {
  desde?: string;
  hasta?: string;
  usuarioId?: number;
}

@Service()
export class AuditoriaService {
  private http = inject(HttpClient);

  listar(pagina: number, tamano: number, filtro?: FiltroAuditoria): Observable<PaginaBackend<RegistroAuditoria>> {
    const params: Record<string, string> = { page: String(pagina), size: String(tamano) };
    if (filtro?.desde) params['desde'] = filtro.desde;
    if (filtro?.hasta) params['hasta'] = filtro.hasta;
    if (filtro?.usuarioId) params['usuarioId'] = String(filtro.usuarioId);
    return this.http.get<PaginaBackend<RegistroAuditoria>>(`${API_URL}/auditoria`, { params });
  }
}
