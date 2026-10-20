import { Component, effect, inject, input, output, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Venta } from '../../models/venta.model';
import { VentaService } from '../../services/venta';
import { formatearMetodoPago } from '../metodo-pago';
import { Icon } from '../icon/icon';
import { Spinner } from '../spinner/spinner';
import { ImpresionTermica } from '../impresion/impresion-termica';
import { ToastService } from '../toast/toast';

@Component({
  imports: [CurrencyPipe, DatePipe, Icon, Spinner],
  selector: 'app-ticket',
  styleUrl: './ticket.scss',
  templateUrl: './ticket.html',
})
export class Ticket {
  private ventaService = inject(VentaService);
  private impresionTermica = inject(ImpresionTermica);
  private toast = inject(ToastService);

  venta = input.required<Venta>();
  autoImprimir = input(false);
  cerrar = output<void>();

  formatearMetodoPago = formatearMetodoPago;

  mostrarConfigImpresora = signal(false);
  cargandoImpresoras = signal(false);
  impresorasDisponibles = signal<string[]>([]);
  impresoraConfigurada = signal(this.impresionTermica.impresoraConfigurada());

  constructor() {
    effect(() => {
      if (this.autoImprimir()) {
        queueMicrotask(() => this.imprimir());
      }
    });
  }

  precioUnitario(item: Venta['items'][number]): number {
    return (
      item.precioUnitarioMomento +
      item.adicionales.reduce((acc, a) => acc + a.precioAdicionalMomento, 0)
    );
  }

  subtotal(item: Venta['items'][number]): number {
    return item.cantidad * this.precioUnitario(item);
  }

  nombresAdicionales(item: Venta['items'][number]): string {
    return item.adicionales.map((a) => a.nombreMomento).join(', ');
  }

  async imprimir(): Promise<void> {
    const impresoPorTermica = await this.impresionTermica.imprimirTicket(this.venta());
    if (!impresoPorTermica) {
      window.print();
    }
    this.ventaService.marcarImpresa(this.venta().id).subscribe();
  }

  async abrirConfigImpresora(): Promise<void> {
    this.mostrarConfigImpresora.set(true);
    this.cargandoImpresoras.set(true);
    try {
      this.impresorasDisponibles.set(await this.impresionTermica.listarImpresoras());
    } catch {
      this.toast.error(
        'No se encontró QZ Tray corriendo en esta máquina. Instalalo y dejalo abierto para imprimir por la térmica.',
      );
      this.impresorasDisponibles.set([]);
    } finally {
      this.cargandoImpresoras.set(false);
    }
  }

  seleccionarImpresora(nombre: string): void {
    this.impresionTermica.configurarImpresora(nombre);
    this.impresoraConfigurada.set(nombre);
    this.mostrarConfigImpresora.set(false);
    this.toast.exito(`Impresora térmica configurada: ${nombre}`);
  }

  quitarImpresora(): void {
    this.impresionTermica.configurarImpresora(null);
    this.impresoraConfigurada.set(null);
    this.mostrarConfigImpresora.set(false);
  }
}
