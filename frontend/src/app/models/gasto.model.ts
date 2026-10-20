export interface Gasto {
  id: number;
  categoria?: string;
  descripcion: string;
  monto: number;
  metodoPago?: string;
  observacion?: string;
  usuarioNombreMomento?: string;
  fecha: string;
}

export interface GastoRequest {
  categoria?: string;
  descripcion: string;
  monto: number;
  metodoPago?: string;
  observacion?: string;
}
