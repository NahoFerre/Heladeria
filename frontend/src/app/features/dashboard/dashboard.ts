import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../services/reporte';
import { CategoriaRanking, ProductoRanking, ReporteGanancia, VentaPorUsuario } from '../../models/reporte.model';
import { Producto } from '../../models/producto.model';
import { fechaLocalIso } from '../../shared/fecha';
import { exportarCsv } from '../../shared/csv-export';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

const TAMANO_PAGINA = 8;

function primerDiaDelMes(): string {
  const hoy = new Date();
  return fechaLocalIso(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
}

@Component({
  imports: [CurrencyPipe, FormsModule, Spinner, Icon, Paginacion],
  selector: 'app-dashboard',
  styleUrl: './dashboard.scss',
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit {
  private reporteService = inject(ReporteService);

  desde = signal(primerDiaDelMes());
  hasta = signal(fechaLocalIso());

  ganancia = signal<ReporteGanancia | null>(null);
  ranking = signal<ProductoRanking[]>([]);
  rankingCategorias = signal<CategoriaRanking[]>([]);
  stockBajo = signal<Producto[]>([]);
  ventasPorUsuario = signal<VentaPorUsuario[]>([]);
  cargando = signal(false);

  paginaVentasPorUsuario = signal(1);
  totalPaginasVentasPorUsuario = computed(() => Math.max(1, Math.ceil(this.ventasPorUsuario().length / TAMANO_PAGINA)));

  paginaRanking = signal(1);
  totalPaginasRanking = computed(() => Math.max(1, Math.ceil(this.ranking().length / TAMANO_PAGINA)));

  paginaRankingCategorias = signal(1);
  totalPaginasRankingCategorias = computed(() => Math.max(1, Math.ceil(this.rankingCategorias().length / TAMANO_PAGINA)));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.paginaVentasPorUsuario.set(1);
    this.paginaRanking.set(1);
    this.paginaRankingCategorias.set(1);
    let pendientes = 5;
    const marcarListo = () => {
      pendientes--;
      if (pendientes === 0) this.cargando.set(false);
    };

    this.reporteService.ganancias(this.desde(), this.hasta()).subscribe((g) => {
      this.ganancia.set(g);
      marcarListo();
    });
    this.reporteService.productosMasRentables(this.desde(), this.hasta()).subscribe((r) => {
      this.ranking.set(r);
      marcarListo();
    });
    this.reporteService.categoriasMasRentables(this.desde(), this.hasta()).subscribe((c) => {
      this.rankingCategorias.set(c);
      marcarListo();
    });
    this.reporteService.stockBajo().subscribe((s) => {
      this.stockBajo.set(s);
      marcarListo();
    });
    this.reporteService.ventasPorUsuario(this.desde(), this.hasta()).subscribe((v) => {
      this.ventasPorUsuario.set(v);
      marcarListo();
    });
  }

  // Controla qué filas se ven en pantalla (paginadas); al imprimir/exportar, el CSS (@media print)
  // vuelve a mostrar todas las filas para que el PDF tenga el reporte completo.
  estaEnPaginaVentasPorUsuario(indice: number): boolean {
    const inicio = (this.paginaVentasPorUsuario() - 1) * TAMANO_PAGINA;
    return indice >= inicio && indice < inicio + TAMANO_PAGINA;
  }

  estaEnPaginaRanking(indice: number): boolean {
    const inicio = (this.paginaRanking() - 1) * TAMANO_PAGINA;
    return indice >= inicio && indice < inicio + TAMANO_PAGINA;
  }

  estaEnPaginaRankingCategorias(indice: number): boolean {
    const inicio = (this.paginaRankingCategorias() - 1) * TAMANO_PAGINA;
    return indice >= inicio && indice < inicio + TAMANO_PAGINA;
  }

  paginaAnteriorVentasPorUsuario(): void {
    this.paginaVentasPorUsuario.update((p) => Math.max(1, p - 1));
  }

  paginaSiguienteVentasPorUsuario(): void {
    this.paginaVentasPorUsuario.update((p) => Math.min(this.totalPaginasVentasPorUsuario(), p + 1));
  }

  paginaAnteriorRanking(): void {
    this.paginaRanking.update((p) => Math.max(1, p - 1));
  }

  paginaSiguienteRanking(): void {
    this.paginaRanking.update((p) => Math.min(this.totalPaginasRanking(), p + 1));
  }

  paginaAnteriorRankingCategorias(): void {
    this.paginaRankingCategorias.update((p) => Math.max(1, p - 1));
  }

  paginaSiguienteRankingCategorias(): void {
    this.paginaRankingCategorias.update((p) => Math.min(this.totalPaginasRankingCategorias(), p + 1));
  }

  exportarPdf(): void {
    window.print();
  }

  exportarVentasPorUsuarioCsv(): void {
    exportarCsv(
      `ventas-por-vendedor_${this.desde()}_${this.hasta()}.csv`,
      [
        { encabezado: 'Vendedor', valor: (v: VentaPorUsuario) => v.usuario },
        { encabezado: 'Cantidad de ventas', valor: (v: VentaPorUsuario) => v.cantidadVentas },
        { encabezado: 'Total vendido', valor: (v: VentaPorUsuario) => v.totalVendido },
      ],
      this.ventasPorUsuario(),
    );
  }

  exportarProductosCsv(): void {
    exportarCsv(
      `productos-mas-rentables_${this.desde()}_${this.hasta()}.csv`,
      [
        { encabezado: 'Producto', valor: (p: ProductoRanking) => p.nombre },
        { encabezado: 'Cantidad vendida', valor: (p: ProductoRanking) => p.cantidadVendida },
        { encabezado: 'Ingresos', valor: (p: ProductoRanking) => p.ingresos },
        { encabezado: 'Ganancia', valor: (p: ProductoRanking) => p.ganancia },
      ],
      this.ranking(),
    );
  }

  exportarCategoriasCsv(): void {
    exportarCsv(
      `categorias-mas-rentables_${this.desde()}_${this.hasta()}.csv`,
      [
        { encabezado: 'Categoría', valor: (c: CategoriaRanking) => c.categoria },
        { encabezado: 'Cantidad vendida', valor: (c: CategoriaRanking) => c.cantidadVendida },
        { encabezado: 'Ingresos', valor: (c: CategoriaRanking) => c.ingresos },
        { encabezado: 'Ganancia', valor: (c: CategoriaRanking) => c.ganancia },
      ],
      this.rankingCategorias(),
    );
  }
}
