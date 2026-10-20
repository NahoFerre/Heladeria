import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../services/reporte';
import { CompraService } from '../../services/compra';
import { MermaService } from '../../services/merma';
import { GastoService } from '../../services/gasto';
import { UsuarioService } from '../../services/usuario';
import { ProductoService } from '../../services/producto';
import { CajaService } from '../../services/caja';
import { MovimientoInventarioReporte, VentaReporte } from '../../models/reporte.model';
import { TotalPorMetodoPago } from '../../models/caja.model';
import { Compra } from '../../models/compra.model';
import { Merma } from '../../models/merma.model';
import { Gasto } from '../../models/gasto.model';
import { Usuario } from '../../models/usuario.model';
import { Producto } from '../../models/producto.model';
import { fechaLocalIso } from '../../shared/fecha';
import { exportarCsv } from '../../shared/csv-export';
import { formatearMetodoPago } from '../../shared/metodo-pago';
import { formatearMotivoMerma } from '../../shared/motivo-merma';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

export type TabReporte = 'ventas' | 'metodos-pago' | 'inventario' | 'compras' | 'perdidas' | 'gastos';

interface CajaOpcion {
  id: number;
  etiqueta: string;
}

const TAMANO_PAGINA = 10;

function primerDiaDelMes(): string {
  const hoy = new Date();
  return fechaLocalIso(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
}

@Component({
  imports: [CurrencyPipe, DatePipe, FormsModule, Spinner, Icon, Paginacion],
  selector: 'app-reportes',
  styleUrl: './reportes.scss',
  templateUrl: './reportes.html',
})
export class Reportes implements OnInit {
  private reporteService = inject(ReporteService);
  private compraService = inject(CompraService);
  private mermaService = inject(MermaService);
  private gastoService = inject(GastoService);
  private usuarioService = inject(UsuarioService);
  private productoService = inject(ProductoService);
  private cajaService = inject(CajaService);

  tab = signal<TabReporte>('ventas');

  desde = signal(primerDiaDelMes());
  hasta = signal(fechaLocalIso());
  usuarioId = signal<number | null>(null);
  metodoPago = signal('');
  cajaId = signal<number | null>(null);
  productoId = signal<number | null>(null);
  categoria = signal('');

  usuarios = signal<Usuario[]>([]);
  productos = signal<Producto[]>([]);
  cajas = signal<CajaOpcion[]>([]);

  cargando = signal(false);

  ventas = signal<VentaReporte[]>([]);
  metodosPago = signal<TotalPorMetodoPago[]>([]);
  inventario = signal<MovimientoInventarioReporte[]>([]);
  compras = signal<Compra[]>([]);
  mermas = signal<Merma[]>([]);
  gastos = signal<Gasto[]>([]);

  pagina = signal(1);

  formatearMetodoPago = formatearMetodoPago;
  formatearMotivoMerma = formatearMotivoMerma;

  totalFilas = computed(() => {
    switch (this.tab()) {
      case 'ventas':
        return this.ventas().length;
      case 'metodos-pago':
        return this.metodosPago().length;
      case 'inventario':
        return this.inventario().length;
      case 'compras':
        return this.compras().length;
      case 'perdidas':
        return this.mermas().length;
      case 'gastos':
        return this.gastos().length;
    }
  });

  totalPaginas = computed(() => Math.max(1, Math.ceil(this.totalFilas() / TAMANO_PAGINA)));

  ngOnInit(): void {
    this.usuarioService.listar().subscribe((usuarios) => this.usuarios.set(usuarios));
    this.productoService.listar(true).subscribe((productos) => this.productos.set(productos));
    this.cargarCajas();
    this.cargar();
  }

  private cargarCajas(): void {
    this.cajaService.historial().subscribe((historial) => {
      const cerradas: CajaOpcion[] = historial.map((c) => ({
        id: c.id,
        etiqueta: `#${c.id} — ${new Date(c.abiertaEn).toLocaleDateString('es-PY')}`,
      }));
      this.cajaService.actual().subscribe((actual) => {
        const abierta: CajaOpcion[] = actual ? [{ id: actual.id, etiqueta: `#${actual.id} — abierta ahora` }] : [];
        this.cajas.set([...abierta, ...cerradas]);
      });
    });
  }

  cambiarTab(tab: TabReporte): void {
    this.tab.set(tab);
    this.pagina.set(1);
    this.cargar();
  }

  aplicarFiltros(): void {
    this.pagina.set(1);
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    const listo = () => this.cargando.set(false);
    const filtro = {
      desde: this.desde() || undefined,
      hasta: this.hasta() || undefined,
      usuarioId: this.usuarioId() ?? undefined,
      metodoPago: this.metodoPago() || undefined,
      cajaId: this.cajaId() ?? undefined,
      productoId: this.productoId() ?? undefined,
      categoria: this.categoria() || undefined,
    };

    switch (this.tab()) {
      case 'ventas':
        this.reporteService.ventas(filtro).subscribe({
          next: (v) => {
            this.ventas.set(v);
            listo();
          },
          error: listo,
        });
        break;
      case 'metodos-pago':
        this.reporteService.metodosPago(filtro).subscribe({
          next: (v) => {
            this.metodosPago.set(v);
            listo();
          },
          error: listo,
        });
        break;
      case 'inventario':
        this.reporteService.inventarioMovimientos(filtro).subscribe({
          next: (v) => {
            this.inventario.set(v);
            listo();
          },
          error: listo,
        });
        break;
      case 'compras':
        this.compraService.listar(filtro).subscribe({
          next: (v) => {
            this.compras.set(v);
            listo();
          },
          error: listo,
        });
        break;
      case 'perdidas':
        this.mermaService.listar(filtro).subscribe({
          next: (v) => {
            this.mermas.set(v);
            listo();
          },
          error: listo,
        });
        break;
      case 'gastos':
        this.gastoService.listar(filtro).subscribe({
          next: (v) => {
            this.gastos.set(v);
            listo();
          },
          error: listo,
        });
        break;
    }
  }

  // Controla qué filas se ven en pantalla (paginadas); al imprimir/exportar PDF, el CSS (@media print)
  // vuelve a mostrar todas las filas para que el PDF tenga el reporte completo.
  estaEnPagina(indice: number): boolean {
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

  exportarCsvActual(): void {
    const nombreArchivo = `${this.tab()}_${this.desde()}_${this.hasta()}.csv`;
    const fechaHora = (iso: string) => new Date(iso).toLocaleString('es-PY');

    switch (this.tab()) {
      case 'ventas':
        exportarCsv<VentaReporte>(
          nombreArchivo,
          [
            { encabezado: 'Fecha', valor: (v) => fechaHora(v.fecha) },
            { encabezado: 'Usuario', valor: (v) => v.usuario },
            { encabezado: 'Método de pago', valor: (v) => formatearMetodoPago(v.metodoPago) },
            { encabezado: 'Total', valor: (v) => v.total },
            { encabezado: 'Caja', valor: (v) => v.cajaId },
          ],
          this.ventas(),
        );
        break;
      case 'metodos-pago':
        exportarCsv<TotalPorMetodoPago>(
          nombreArchivo,
          [
            { encabezado: 'Método de pago', valor: (m) => formatearMetodoPago(m.metodoPago) },
            { encabezado: 'Cantidad de ventas', valor: (m) => m.cantidadVentas },
            { encabezado: 'Total vendido', valor: (m) => m.totalVendido },
          ],
          this.metodosPago(),
        );
        break;
      case 'inventario':
        exportarCsv<MovimientoInventarioReporte>(
          nombreArchivo,
          [
            { encabezado: 'Fecha', valor: (m) => fechaHora(m.fecha) },
            { encabezado: 'Producto', valor: (m) => m.producto },
            { encabezado: 'Tipo', valor: (m) => m.tipo },
            { encabezado: 'Cantidad', valor: (m) => m.cantidad },
            { encabezado: 'Motivo', valor: (m) => m.motivo ?? '' },
          ],
          this.inventario(),
        );
        break;
      case 'compras':
        exportarCsv<Compra>(
          nombreArchivo,
          [
            { encabezado: 'Fecha', valor: (c) => fechaHora(c.fecha) },
            { encabezado: 'Proveedor', valor: (c) => c.proveedor ?? '' },
            { encabezado: 'Nº factura', valor: (c) => c.numeroFactura ?? '' },
            { encabezado: 'Método de pago', valor: (c) => (c.metodoPago ? formatearMetodoPago(c.metodoPago) : '') },
            { encabezado: 'Usuario', valor: (c) => c.usuarioNombreMomento ?? '' },
            { encabezado: 'Total', valor: (c) => c.total },
          ],
          this.compras(),
        );
        break;
      case 'perdidas':
        exportarCsv<Merma>(
          nombreArchivo,
          [
            { encabezado: 'Fecha', valor: (m) => fechaHora(m.fecha) },
            { encabezado: 'Ítem', valor: (m) => m.nombreMomento },
            { encabezado: 'Cantidad', valor: (m) => m.cantidad },
            { encabezado: 'Motivo', valor: (m) => formatearMotivoMerma(m.motivo) },
            { encabezado: 'Costo total', valor: (m) => m.costoTotal },
            { encabezado: 'Usuario', valor: (m) => m.usuarioNombreMomento ?? '' },
          ],
          this.mermas(),
        );
        break;
      case 'gastos':
        exportarCsv<Gasto>(
          nombreArchivo,
          [
            { encabezado: 'Fecha', valor: (g) => fechaHora(g.fecha) },
            { encabezado: 'Categoría', valor: (g) => g.categoria ?? '' },
            { encabezado: 'Descripción', valor: (g) => g.descripcion },
            { encabezado: 'Monto', valor: (g) => g.monto },
            { encabezado: 'Método de pago', valor: (g) => (g.metodoPago ? formatearMetodoPago(g.metodoPago) : '') },
            { encabezado: 'Usuario', valor: (g) => g.usuarioNombreMomento ?? '' },
          ],
          this.gastos(),
        );
        break;
    }
  }
}
