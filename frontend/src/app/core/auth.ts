import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api-config';
import { firstValueFrom } from 'rxjs';
import { Usuario } from '../models/usuario.model';

const STORAGE_KEY = 'heladeria_auth';
const USER_KEY = 'heladeria_usuario';

@Service()
export class Auth {
  private http = inject(HttpClient);

  // Lanza el error tal cual (HttpErrorResponse) en vez de devolver false: así el llamador puede
  // distinguir "usuario/contraseña incorrectos" de "no hay conexión con el servidor", que son
  // situaciones bien distintas para el usuario aunque las dos hagan fallar el login.
  async login(username: string, password: string): Promise<Usuario> {
    const credentials = btoa(`${username}:${password}`);
    // Además de validar las credenciales, trae el rol del usuario para armar el menú
    const usuario = await firstValueFrom(
      this.http.get<Usuario>(`${API_URL}/usuarios/yo`, {
        headers: { Authorization: `Basic ${credentials}` },
      }),
    );
    localStorage.setItem(STORAGE_KEY, credentials);
    localStorage.setItem(USER_KEY, JSON.stringify(usuario));
    return usuario;
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(USER_KEY);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(STORAGE_KEY);
  }

  getAuthHeader(): string | null {
    const credentials = localStorage.getItem(STORAGE_KEY);
    return credentials ? `Basic ${credentials}` : null;
  }

  getUsuario(): Usuario | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  esAdmin(): boolean {
    return this.getUsuario()?.rol === 'ADMIN';
  }
}
