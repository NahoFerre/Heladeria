import { Service, signal } from '@angular/core';

export type TipoToast = 'exito' | 'error';

export interface Toast {
  id: number;
  tipo: TipoToast;
  mensaje: string;
}

const DURACION_MS = 4000;

@Service()
export class ToastService {
  private toasts = signal<Toast[]>([]);
  lista = this.toasts.asReadonly();

  private siguienteId = 0;

  exito(mensaje: string): void {
    this.mostrar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.mostrar('error', mensaje);
  }

  cerrar(id: number): void {
    this.toasts.update((lista) => lista.filter((toast) => toast.id !== id));
  }

  private mostrar(tipo: TipoToast, mensaje: string): void {
    const id = this.siguienteId++;
    this.toasts.update((lista) => [...lista, { id, tipo, mensaje }]);
    setTimeout(() => this.cerrar(id), DURACION_MS);
  }
}
