import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MermaService } from '../../services/merma';
import { InsumoService } from '../../services/insumo';
import { ProductoService } from '../../services/producto';
import { Insumo } from '../../models/insumo.model';
import { Producto } from '../../models/producto.model';
import { Merma, MermaRequest, MotivoMerma } from '../../models/merma.model';
import { formatearUnidadMedida } from '../../shared/unidad-medida';
import { ETIQUETAS_MOTIVO_MERMA, formatearMotivoMerma } from '../../shared/motivo-merma';
import { paginar, totalPaginas } from '../../shared/pagination';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

type TipoItemMerma = 'INSUMO' | 'PRODUCTO';

const TAMANO_PAGINA = 8;

const MOTIVOS: MotivoMerma[] = Object.keys(ETIQUETAS_MOTIVO_MERMA) as MotivoMerma[];

interface ResumenFila {
  nombre: string;
  costoTotal: number;
}

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe, Spinner, Icon, Paginacion],
  selector: 'app-mermas',
  styleUrl: './mermas.scss',
  templateUrl: './mermas.html',
})
export class Mermas implements OnInit {
  private mermaService = inject(MermaService);
  private insumoService = inject(InsumoService);
  private productoService = inject(ProductoService);
  private toast = inject(ToastService);

  insumos = signal<Insumo[]>([]);
  productos = signal<Producto[]>([]);

  tipoItem = signal<TipoItemMerma>('INSUMO');
  insumoIdItem = signal<number | null>(null);
  productoIdItem = signal<number | null>(null);
  cantidadItem = signal(0);
  motivoItem = signal<MotivoMerma>('PRODUCTO_DERRETIDO');
  observacionItem = signal('');
  procesando = signal(false);

  mermas = signal<Merma[]>([]);
  cargando = signal(true);
  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.mermas().length, TAMANO_PAGINA));
  mermasPagina = computed(() => paginar(this.mermas(), this.pagina(), TAMANO_PAGINA));

  costoTotal = computed(() => this.mermas().reduce((acc, m) => acc + m.costoTotal, 0));
  porItem = computed(() => this.agrupar((m) => m.nombreMomento));
  porMotivo = computed(() => this.agrupar((m) => this.etiquetaMotivo(m.motivo)));
  porUsuario = computed(() => this.agrupar((m) => m.usuarioNombreMomento ?? '—'));

  motivos = MOTIVOS;
  formatearUnidadMedida = formatearUnidadMedida;

  ngOnInit(): void {
    this.insumoService.listar(true).subscribe((insumos) => this.insumos.set(insumos));
    this.productoService.listar(true).subscribe((productos) => this.productos.set(productos));
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.mermaService.listar().subscribe({
      next: (mermas) => {
        this.cargando.set(false);
        this.mermas.set(mermas);
        this.pagina.set(1);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudo cargar el historial de pérdidas');
      },
    });
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  etiquetaMotivo(motivo: MotivoMerma): string {
    return formatearMotivoMerma(motivo);
  }

  registrarMerma(): void {
    const cantidad = this.cantidadItem();
    if (cantidad <= 0) {
      this.toast.error('La cantidad debe ser mayor a cero');
      return;
    }

    const request: MermaRequest = {
      cantidad,
      motivo: this.motivoItem(),
      observacion: this.observacionItem() || undefined,
    };

    if (this.tipoItem() === 'INSUMO') {
      if (!this.insumoIdItem()) {
        this.toast.error('Elegí un insumo');
        return;
      }
      request.insumoId = this.insumoIdItem()!;
    } else {
      if (!this.productoIdItem()) {
        this.toast.error('Elegí un producto');
        return;
      }
      request.productoId = this.productoIdItem()!;
    }

    this.procesando.set(true);
    this.mermaService.crear(request).subscribe({
      next: () => {
        this.procesando.set(false);
        this.limpiarFormulario();
        this.cargar();
        this.toast.exito('Pérdida registrada correctamente');
      },
      error: (err) => {
        this.procesando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo registrar la pérdida');
      },
    });
  }

  private limpiarFormulario(): void {
    this.insumoIdItem.set(null);
    this.productoIdItem.set(null);
    this.cantidadItem.set(0);
    this.motivoItem.set('PRODUCTO_DERRETIDO');
    this.observacionItem.set('');
  }

  private agrupar(clave: (m: Merma) => string): ResumenFila[] {
    const mapa = new Map<string, number>();
    for (const merma of this.mermas()) {
      const k = clave(merma);
      mapa.set(k, (mapa.get(k) ?? 0) + merma.costoTotal);
    }
    return [...mapa.entries()]
      .map(([nombre, costoTotal]) => ({ nombre, costoTotal }))
      .sort((a, b) => b.costoTotal - a.costoTotal);
  }
}
