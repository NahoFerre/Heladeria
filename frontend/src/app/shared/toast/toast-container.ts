import { Component, inject } from '@angular/core';
import { ToastService } from './toast';

@Component({
  selector: 'app-toast-container',
  styleUrl: './toast-container.scss',
  templateUrl: './toast-container.html',
})
export class ToastContainer {
  private toastService = inject(ToastService);
  toasts = this.toastService.lista;

  cerrar(id: number): void {
    this.toastService.cerrar(id);
  }
}
