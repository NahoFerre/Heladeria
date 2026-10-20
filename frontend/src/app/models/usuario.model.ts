export type Rol = 'ADMIN' | 'CAJERO';

export interface Usuario {
  id: number;
  username: string;
  rol: Rol;
}

export interface UsuarioRequest {
  username: string;
  password: string;
  rol: Rol;
}
