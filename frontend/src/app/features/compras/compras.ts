import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CompraService } from '../../services/compra';
import { InsumoService } from '../../services/insumo';
import { ProductoService } from '../../services/producto';
import { Insumo } from '../../models/insumo.model';
import { Producto } from '../../models/producto.model';
import { Compra, CompraRequest } from '../../models/compra.model';
import { formatearUnidadMedida } from '../../shared/unidad-medida';
import { paginar, totalPaginas } from '../../shared/pagination';
import { idUnico } from '../../shared/id-unico';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';
import { MontoInput } from '../../shared/monto-input/monto-input';

type TipoItemCompra = 'INSUMO' | 'PRODUCTO';

interface ItemCompraForm {
  lineId: string;
  tipo: TipoItemCompra;
  insumoId?: number;
  productoId?: number;
  nombre: string;
  unidad?: string;
  cantidad: number;
  costoUnitario: number;
}

const TAMANO_PAGINA = 8;

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe, Spinner, Icon, Paginacion, MontoInput],
  selector: 'app-compras',
  styleUrl: './compras.scss',
  templateUrl: './compras.html',
})
export class Compras implements OnInit {
  private compraService = inject(CompraService);
  private insumoService = inject(InsumoService);
  private productoService = inject(ProductoService);
  private toast = inject(ToastService);

  insumos = signal<Insumo[]>([]);
  productos = signal<Producto[]>([]);

  proveedor = signal('');
  numeroFactura = signal('');
  metodoPago = signal('');
  observaciones = signal('');

  tipoItem = signal<TipoItemCompra>('INSUMO');
  insumoIdItem = signal<number | null>(null);
  productoIdItem = signal<number | null>(null);
  cantidadItem = signal(0);
  costoUnitarioItem = signal(0);

  items = signal<ItemCompraForm[]>([]);
  total = computed(() => this.items().reduce((acc, item) => acc + item.cantidad * item.costoUnitario, 0));
  procesando = signal(false);

  compras = signal<Compra[]>([]);
  cargandoHistorial = signal(true);
  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.compras().length, TAMANO_PAGINA));
  comprasPagina = computed(() => paginar(this.compras(), this.pagina(), TAMANO_PAGINA));

  compraSeleccionada = signal<Compra | null>(null);

  formatearUnidadMedida = formatearUnidadMedida;

  ngOnInit(): void {
    this.insumoService.listar(true).subscribe((insumos) => this.insumos.set(insumos));
    this.productoService.listar(true).subscribe((productos) => this.productos.set(productos));
    this.cargarHistorial();
  }

  cargarHistorial(): void {
    this.cargandoHistorial.set(true);
    this.compraService.listar().subscribe({
      next: (compras) => {
        this.cargandoHistorial.set(false);
        this.compras.set(compras);
        this.pagina.set(1);
      },
      error: () => {
        this.cargandoHistorial.set(false);
        this.toast.error('No se pudo cargar el historial de compras');
      },
    });
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  seleccionarInsumo(idTexto: string): void {
    const id = Number(idTexto);
    this.insumoIdItem.set(id || null);
    const insumo = this.insumos().find((i) => i.id === id);
    if (insumo) {
      this.costoUnitarioItem.set(insumo.costoUnitario);
    }
  }

  seleccionarProducto(idTexto: string): void {
    const id = Number(idTexto);
    this.productoIdItem.set(id || null);
    const producto = this.productos().find((p) => p.id === id);
    if (producto) {
      this.costoUnitarioItem.set(producto.costoUnitario);
    }
  }

  agregarItem(): void {
    const cantidad = this.cantidadItem();
    const costoUnitario = this.costoUnitarioItem();
    if (cantidad <= 0) {
      this.toast.error('La cantidad debe ser mayor a cero');
      return;
    }

    if (this.tipoItem() === 'INSUMO') {
      const insumo = this.insumos().find((i) => i.id === this.insumoIdItem());
      if (!insumo) {
        this.toast.error('Elegí un insumo');
        return;
      }
      this.items.update((lista) => [
        ...lista,
        {
          lineId: idUnico(),
          tipo: 'INSUMO',
          insumoId: insumo.id!,
          nombre: insumo.nombre,
          unidad: formatearUnidadMedida(insumo.unidadMedida),
          cantidad,
          costoUnitario,
        },
      ]);
    } else {
      const producto = this.productos().find((p) => p.id === this.productoIdItem());
      if (!producto) {
        this.toast.error('Elegí un producto');
        return;
      }
      this.items.update((lista) => [
        ...lista,
        {
          lineId: idUnico(),
          tipo: 'PRODUCTO',
          productoId: producto.id!,
          nombre: producto.nombre,
          cantidad,
          costoUnitario,
        },
      ]);
    }

    this.cantidadItem.set(0);
    this.costoUnitarioItem.set(0);
  }

  quitarItem(lineId: string): void {
    this.items.update((lista) => lista.filter((item) => item.lineId !== lineId));
  }

  registrarCompra(): void {
    if (this.items().length === 0) {
      this.toast.error('Agregá al menos un ítem a la compra');
      return;
    }

    const request: CompraRequest = {
      proveedor: this.proveedor() || undefined,
      numeroFactura: this.numeroFactura() || undefined,
      metodoPago: this.metodoPago() || undefined,
      observaciones: this.observaciones() || undefined,
      items: this.items().map((item) => ({
        insumoId: item.insumoId,
        productoId: item.productoId,
        cantidad: item.cantidad,
        costoUnitario: item.costoUnitario,
      })),
    };

    this.procesando.set(true);
    this.compraService.crear(request).subscribe({
      next: () => {
        this.procesando.set(false);
        this.limpiarFormulario();
        this.cargarHistorial();
        this.toast.exito('Compra registrada correctamente');
      },
      error: (err) => {
        this.procesando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo registrar la compra');
      },
    });
  }

  limpiarFormulario(): void {
    this.proveedor.set('');
    this.numeroFactura.set('');
    this.metodoPago.set('');
    this.observaciones.set('');
    this.items.set([]);
    this.insumoIdItem.set(null);
    this.productoIdItem.set(null);
    this.cantidadItem.set(0);
    this.costoUnitarioItem.set(0);
  }

  verDetalle(compra: Compra): void {
    this.compraSeleccionada.set(compra);
  }

  cerrarDetalle(): void {
    this.compraSeleccionada.set(null);
  }
}
