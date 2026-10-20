import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VentaService } from '../../services/venta';
import { Venta } from '../../models/venta.model';
import { Auth } from '../../core/auth';
import { ToastService } from '../../shared/toast/toast';
import { formatearMetodoPago } from '../../shared/metodo-pago';
import { paginar, totalPaginas } from '../../shared/pagination';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';
import { Ticket } from '../../shared/ticket/ticket';

const TAMANO_PAGINA = 8;

@Component({
  imports: [CurrencyPipe, DatePipe, FormsModule, Spinner, Icon, Paginacion, Ticket],
  selector: 'app-registro-ventas',
  styleUrl: './registro-ventas.scss',
  templateUrl: './registro-ventas.html',
})
export class RegistroVentas implements OnInit {
  private ventaService = inject(VentaService);
  private auth = inject(Auth);
  private toast = inject(ToastService);

  ventas = signal<Venta[]>([]);
  cargando = signal(true);

  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.ventas().length, TAMANO_PAGINA));
  ventasPagina = computed(() => paginar(this.ventas(), this.pagina(), TAMANO_PAGINA));

  ventaSeleccionada = signal<Venta | null>(null);
  autoImprimir = signal(false);

  ventaAAnular = signal<Venta | null>(null);
  motivoAnulacion = signal('');
  anulando = signal(false);

  esAdmin = this.auth.esAdmin();

  formatearMetodoPago = formatearMetodoPago;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.ventaService.listar().subscribe({
      next: (ventas) => {
        this.cargando.set(false);
        this.ventas.set(ventas);
      },
      error: () => this.cargando.set(false),
    });
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  verTicket(venta: Venta): void {
    this.autoImprimir.set(false);
    this.ventaSeleccionada.set(venta);
  }

  imprimirVenta(venta: Venta): void {
    this.autoImprimir.set(true);
    this.ventaSeleccionada.set(venta);
  }

  cerrarTicket(): void {
    this.ventaSeleccionada.set(null);
    this.cargar();
  }

  abrirAnular(venta: Venta): void {
    this.ventaAAnular.set(venta);
    this.motivoAnulacion.set('');
  }

  cerrarAnular(): void {
    this.ventaAAnular.set(null);
  }

  confirmarAnular(): void {
    const venta = this.ventaAAnular();
    const motivo = this.motivoAnulacion().trim();
    if (!venta || !motivo) {
      return;
    }

    this.anulando.set(true);
    this.ventaService.anular(venta.id, motivo).subscribe({
      next: () => {
        this.anulando.set(false);
        this.ventaAAnular.set(null);
        this.toast.exito('Venta anulada correctamente');
        this.cargar();
      },
      error: (err) => {
        this.anulando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo anular la venta');
      },
    });
  }
}
