import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CajaService } from '../../services/caja';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { MontoInput } from '../../shared/monto-input/monto-input';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';
import { paginar, totalPaginas as calcularTotalPaginas } from '../../shared/pagination';
import { CajaActual, CierreCaja, MovimientoCaja, TipoMovimientoCaja } from '../../models/caja.model';
import { formatearMetodoPago } from '../../shared/metodo-pago';

const TAMANO_PAGINA_MOVIMIENTOS = 5;

@Component({
  imports: [CurrencyPipe, DatePipe, FormsModule, Spinner, MontoInput, Icon, Paginacion],
  selector: 'app-caja',
  styleUrl: './caja.scss',
  templateUrl: './caja.html',
})
export class Caja implements OnInit {
  private cajaService = inject(CajaService);
  private toast = inject(ToastService);

  cargando = signal(true);
  procesando = signal(false);

  cajaActual = signal<CajaActual | null>(null);

  montoInicial = signal(0);
  montoContado = signal(0);
  observacion = signal('');

  ultimoCierre = signal<CierreCaja | null>(null);
  mostrarResumenCierre = signal(false);

  movimientos = signal<MovimientoCaja[]>([]);
  mostrarFormMovimiento = signal<TipoMovimientoCaja | null>(null);
  montoMovimiento = signal(0);
  motivoMovimiento = signal('');
  procesandoMovimiento = signal(false);

  paginaMovimientos = signal(1);
  totalPaginasMovimientos = computed(() => calcularTotalPaginas(this.movimientos().length, TAMANO_PAGINA_MOVIMIENTOS));
  movimientosPagina = computed(() => paginar(this.movimientos(), this.paginaMovimientos(), TAMANO_PAGINA_MOVIMIENTOS));

  formatearMetodoPago = formatearMetodoPago;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.cajaService.actual().subscribe({
      next: (caja) => {
        this.cargando.set(false);
        this.cajaActual.set(caja);
        if (caja) {
          this.cargarMovimientos();
        }
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudo consultar el estado de la caja');
      },
    });
  }

  cargarMovimientos(): void {
    this.cajaService.movimientosActual().subscribe({
      next: (movimientos) => this.movimientos.set(movimientos),
      error: () => this.toast.error('No se pudieron cargar los movimientos de la caja'),
    });
  }

  abrirCaja(): void {
    this.procesando.set(true);
    this.cajaService.abrir({ montoInicial: this.montoInicial() }).subscribe({
      next: (caja) => {
        this.procesando.set(false);
        this.cajaActual.set(caja);
        this.montoInicial.set(0);
        this.toast.exito('Caja abierta correctamente');
      },
      error: (err) => {
        this.procesando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo abrir la caja');
      },
    });
  }

  cerrarCaja(): void {
    this.procesando.set(true);
    this.cajaService.cerrar({ montoContado: this.montoContado(), observacion: this.observacion() }).subscribe({
      next: (cierre) => {
        this.procesando.set(false);
        this.cajaActual.set(null);
        this.montoContado.set(0);
        this.observacion.set('');
        this.ultimoCierre.set(cierre);
        this.mostrarResumenCierre.set(true);
        this.toast.exito('Caja cerrada correctamente');
      },
      error: (err) => {
        this.procesando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo cerrar la caja');
      },
    });
  }

  cerrarResumen(): void {
    this.mostrarResumenCierre.set(false);
  }

  abrirFormMovimiento(tipo: TipoMovimientoCaja): void {
    this.mostrarFormMovimiento.set(tipo);
    this.montoMovimiento.set(0);
    this.motivoMovimiento.set('');
  }

  cancelarMovimiento(): void {
    this.mostrarFormMovimiento.set(null);
  }

  registrarMovimiento(): void {
    const tipo = this.mostrarFormMovimiento();
    if (!tipo) return;

    this.procesandoMovimiento.set(true);
    this.cajaService.registrarMovimiento({ tipo, monto: this.montoMovimiento(), motivo: this.motivoMovimiento() }).subscribe({
      next: () => {
        this.procesandoMovimiento.set(false);
        this.mostrarFormMovimiento.set(null);
        this.cargar();
        this.toast.exito('Movimiento registrado correctamente');
      },
      error: (err) => {
        this.procesandoMovimiento.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo registrar el movimiento');
      },
    });
  }

  etiquetaMovimiento(tipo: TipoMovimientoCaja): string {
    switch (tipo) {
      case 'RETIRO':
        return 'Retiro';
      case 'INGRESO':
        return 'Ingreso';
      case 'GASTO':
        return 'Gasto';
    }
  }
}
