import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { CajaService } from '../../services/caja';
import { CajaHistorial } from '../../models/caja.model';
import { Spinner } from '../../shared/spinner/spinner';
import { paginar, totalPaginas } from '../../shared/pagination';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

const TAMANO_PAGINA = 8;

@Component({
  imports: [CurrencyPipe, DatePipe, Spinner, Icon, Paginacion],
  selector: 'app-historial-cajas',
  styleUrl: './historial-cajas.scss',
  templateUrl: './historial-cajas.html',
})
export class HistorialCajas implements OnInit {
  private cajaService = inject(CajaService);

  historial = signal<CajaHistorial[]>([]);
  cargando = signal(true);

  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.historial().length, TAMANO_PAGINA));

  totales = computed(() => {
    const lista = this.historial();
    return {
      montoApertura: lista.reduce((acc, c) => acc + c.montoApertura, 0),
      montoCierreEsperado: lista.reduce((acc, c) => acc + c.montoCierreEsperado, 0),
      montoCierreContado: lista.reduce((acc, c) => acc + c.montoCierreContado, 0),
      diferencia: lista.reduce((acc, c) => acc + c.diferencia, 0),
      totalVentas: lista.reduce((acc, c) => acc + c.totalVentas, 0),
      totalIngresos: lista.reduce((acc, c) => acc + c.totalIngresos, 0),
      totalRetiros: lista.reduce((acc, c) => acc + c.totalRetiros, 0),
      totalGastos: lista.reduce((acc, c) => acc + c.totalGastos, 0),
    };
  });

  ngOnInit(): void {
    this.cajaService.historial().subscribe({
      next: (historial) => {
        this.cargando.set(false);
        this.historial.set(historial);
      },
      error: () => this.cargando.set(false),
    });
  }

  // Controla qué filas se ven en pantalla (paginadas); al imprimir/exportar, el CSS
  // (@media print) vuelve a mostrar todas las filas para que el PDF tenga el registro completo.
  estaEnPaginaActual(indice: number): boolean {
    const inicio = (this.pagina() - 1) * TAMANO_PAGINA;
    return indice >= inicio && indice < inicio + TAMANO_PAGINA;
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  exportarPdf(): void {
    window.print();
  }
}
