export interface RegistroAuditoria {
  id: number;
  usuario: { id: number; username: string; rol: string } | null;
  usuarioNombreMomento: string | null;
  accion: string;
  entidad: string | null;
  entidadId: number | null;
  detalle: string | null;
  fecha: string;
}

export interface PaginaBackend<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}
