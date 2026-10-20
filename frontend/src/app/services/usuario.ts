import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { Usuario, UsuarioRequest } from '../models/usuario.model';

@Service()
export class UsuarioService {
  private http = inject(HttpClient);

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${API_URL}/usuarios`);
  }

  crear(usuario: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/usuarios`, usuario);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/usuarios/${id}`);
  }
}
