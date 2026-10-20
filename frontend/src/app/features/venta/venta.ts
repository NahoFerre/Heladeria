import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../../services/producto';
import { VentaService } from '../../services/venta';
import { CajaService } from '../../services/caja';
import { PromocionService } from '../../services/promocion';
import { Adicional, GrupoAdicionales, Producto, ProductoVariante } from '../../models/producto.model';
import { Venta as VentaModel } from '../../models/venta.model';
import { CajaActual } from '../../models/caja.model';
import { Promocion } from '../../models/promocion.model';
import { paginar, totalPaginas } from '../../shared/pagination';
import { mejorPromocionPara, precioConPromocion } from '../../shared/promocion-precio';
import { idUnico } from '../../shared/id-unico';
import { Ticket } from '../../shared/ticket/ticket';
import { ToastService } from '../../shared/toast/toast';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';
import { MontoInput } from '../../shared/monto-input/monto-input';

interface ItemAdicionalSeleccionado {
  adicionalId: number;
  nombre: string;
  precioAdicional: number;
}

interface ItemCarrito {
  lineId: string;
  producto: Producto;
  variante?: ProductoVariante;
  adicionales: ItemAdicionalSeleccionado[];
  cantidad: number;
}

type Columna = 'nombre' | 'categoria' | 'precioVenta' | 'stockActual';

const TAMANO_PAGINA = 8;

function claveDeLinea(productoId: number, varianteId: number | undefined, adicionalIds: number[]): string {
  const idsOrdenados = [...adicionalIds].sort((a, b) => a - b).join(',');
  return `${productoId}|${varianteId ?? ''}|${idsOrdenados}`;
}

@Component({
  imports: [CurrencyPipe, FormsModule, Ticket, RouterLink, Icon, Paginacion, MontoInput],
  selector: 'app-venta',
  styleUrl: './venta.scss',
  templateUrl: './venta.html',
})
export class Venta implements OnInit {
  private productoService = inject(ProductoService);
  private ventaService = inject(VentaService);
  private cajaService = inject(CajaService);
  private promocionService = inject(PromocionService);
  private toast = inject(ToastService);

  cajaAbierta = signal<CajaActual | null>(null);

  productos = signal<Producto[]>([]);
  categorias = signal<string[]>([]);
  promociones = signal<Promocion[]>([]);

  busqueda = signal('');
  categoriaFiltro = signal('');
  columnaOrden = signal<Columna>('nombre');
  direccionOrden = signal<'asc' | 'desc'>('asc');
  pagina = signal(1);

  carrito = signal<ItemCarrito[]>([]);
  metodoPago = signal('EFECTIVO');
  montoRecibido = signal(0);
  procesando = signal(false);

  ultimaVenta = signal<VentaModel | null>(null);
  mostrarTicket = signal(false);

  // Modal de agregar producto (cantidad + variante + adicionales)
  productoModal = signal<Producto | null>(null);
  cantidadModal = signal(1);
  varianteModalId = signal<number | null>(null);
  adicionalesModalIds = signal<Set<number>>(new Set());

  itemsCarrito = this.carrito;
  total = computed(() => this.itemsCarrito().reduce((acc, item) => acc + this.precioUnitario(item) * item.cantidad, 0));
  vuelto = computed(() => this.montoRecibido() - this.total());

  gruposModal = computed(() => this.productoModal()?.gruposAdicionales ?? []);
  varianteSeleccionadaModal = computed(() =>
    this.productoModal()?.variantes?.find((v) => v.id === this.varianteModalId()),
  );
  precioBaseModalSinDescuento = computed(() => this.varianteSeleccionadaModal()?.precio ?? this.productoModal()?.precioVenta ?? 0);
  promocionModal = computed<Promocion | null>(() => {
    const producto = this.productoModal();
    if (!producto) return null;
    return mejorPromocionPara(producto, this.cantidadModal(), this.precioBaseModalSinDescuento(), this.promociones());
  });
  precioBaseModal = computed(() => {
    const promocion = this.promocionModal();
    const base = this.precioBaseModalSinDescuento();
    return promocion ? precioConPromocion(promocion, base) : base;
  });
  totalAdicionalesModal = computed(() =>
    this.gruposModal().reduce(
      (acc, grupo) => acc + grupo.opciones.filter((o) => this.adicionalesModalIds().has(o.id!)).reduce((a, o) => a + o.precioAdicional, 0),
      0,
    ),
  );
  subtotalModal = computed(() => (this.precioBaseModal() + this.totalAdicionalesModal()) * this.cantidadModal());

  stockDisponibleModal = computed<number | null>(() => {
    const variante = this.varianteSeleccionadaModal();
    if (variante) return variante.stockActual;

    const producto = this.productoModal();
    if (!producto) return 0;
    if ((producto.variantes ?? []).some((v) => v.activo)) return null; // todavía no eligió variante
    return producto.stockActual;
  });

  modalValido = computed(() => {
    const producto = this.productoModal();
    const cantidad = this.cantidadModal();
    if (!producto || cantidad <= 0) return false;

    const variantesActivas = (producto.variantes ?? []).filter((v) => v.activo);
    if (variantesActivas.length > 0) {
      const variante = this.varianteSeleccionadaModal();
      if (!variante || cantidad > variante.stockActual) return false;
    } else if (cantidad > producto.stockActual) {
      return false;
    }

    for (const grupo of this.gruposModal()) {
      const cantidadSeleccionada = grupo.opciones.filter((o) => this.adicionalesModalIds().has(o.id!)).length;
      if (cantidadSeleccionada < grupo.minSeleccion || cantidadSeleccionada > grupo.maxSeleccion) return false;
    }
    return true;
  });

  productosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const categoria = this.categoriaFiltro();
    const columna = this.columnaOrden();
    const direccion = this.direccionOrden() === 'asc' ? 1 : -1;

    let lista = this.productos();
    if (texto) {
      lista = lista.filter((p) => p.nombre.toLowerCase().includes(texto));
    }
    if (categoria) {
      lista = lista.filter((p) => p.categoria === categoria);
    }

    return [...lista].sort((a, b) => {
      const va = a[columna] ?? '';
      const vb = b[columna] ?? '';
      if (typeof va === 'string') return va.localeCompare(vb as string) * direccion;
      return ((va as number) - (vb as number)) * direccion;
    });
  });

  totalPaginas = computed(() => totalPaginas(this.productosFiltrados().length, TAMANO_PAGINA));
  productosPagina = computed(() => paginar(this.productosFiltrados(), this.pagina(), TAMANO_PAGINA));

  ngOnInit(): void {
    this.cargarProductos();
    this.productoService.categorias().subscribe((categorias) => this.categorias.set(categorias));
    this.cajaService.actual().subscribe((caja) => this.cajaAbierta.set(caja));
    this.promocionService.vigentes().subscribe((promociones) => this.promociones.set(promociones));
  }

  cargarProductos(): void {
    this.productoService.listar(true).subscribe((productos) => this.productos.set(productos));
  }

  onFiltroCambiado(): void {
    this.pagina.set(1);
  }

  ordenarPor(columna: Columna): void {
    if (this.columnaOrden() === columna) {
      this.direccionOrden.set(this.direccionOrden() === 'asc' ? 'desc' : 'asc');
    } else {
      this.columnaOrden.set(columna);
      this.direccionOrden.set('asc');
    }
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  precioUnitario(item: ItemCarrito): number {
    const base = item.variante?.precio ?? item.producto.precioVenta;
    const promocion = this.promocionAplicada(item);
    const baseConDescuento = promocion ? precioConPromocion(promocion, base) : base;
    return baseConDescuento + item.adicionales.reduce((acc, a) => acc + a.precioAdicional, 0);
  }

  // La promoción vigente que le toca a este renglón del carrito (o null si ninguna aplica). Las
  // promos solo descuentan el precio base del producto/variante, nunca el precio de los adicionales.
  promocionAplicada(item: ItemCarrito): Promocion | null {
    const base = item.variante?.precio ?? item.producto.precioVenta;
    return mejorPromocionPara(item.producto, item.cantidad, base, this.promociones());
  }

  nombresAdicionales(item: ItemCarrito): string {
    return item.adicionales.map((a) => a.nombre).join(', ');
  }

  abrirModalAgregar(producto: Producto): void {
    if (producto.stockActual <= 0 && !(producto.variantes && producto.variantes.length > 0)) return;
    this.productoModal.set(producto);
    this.cantidadModal.set(1);
    this.varianteModalId.set(null);
    this.adicionalesModalIds.set(new Set());
  }

  cerrarModalAgregar(): void {
    this.productoModal.set(null);
  }

  elegirVarianteModal(varianteId: number): void {
    this.varianteModalId.set(varianteId);
  }

  toggleAdicionalModal(grupo: GrupoAdicionales, adicional: Adicional): void {
    const actuales = new Set(this.adicionalesModalIds());
    const idsDelGrupo = new Set(grupo.opciones.map((o) => o.id!));
    const seleccionadosDelGrupo = [...actuales].filter((id) => idsDelGrupo.has(id));

    if (actuales.has(adicional.id!)) {
      actuales.delete(adicional.id!);
    } else if (grupo.maxSeleccion === 1) {
      seleccionadosDelGrupo.forEach((id) => actuales.delete(id));
      actuales.add(adicional.id!);
    } else if (seleccionadosDelGrupo.length < grupo.maxSeleccion) {
      actuales.add(adicional.id!);
    }
    this.adicionalesModalIds.set(actuales);
  }

  confirmarAgregarDesdeModal(): void {
    const producto = this.productoModal();
    const cantidad = this.cantidadModal();
    if (!producto || !this.modalValido()) return;

    const variante = this.varianteSeleccionadaModal();
    const adicionales: ItemAdicionalSeleccionado[] = this.gruposModal()
      .flatMap((g) => g.opciones)
      .filter((o) => this.adicionalesModalIds().has(o.id!))
      .map((o) => ({ adicionalId: o.id!, nombre: o.nombre, precioAdicional: o.precioAdicional }));

    const clave = claveDeLinea(producto.id!, variante?.id, adicionales.map((a) => a.adicionalId));
    const lista = this.carrito();
    const indiceExistente = lista.findIndex(
      (item) => claveDeLinea(item.producto.id!, item.variante?.id, item.adicionales.map((a) => a.adicionalId)) === clave,
    );

    if (indiceExistente >= 0) {
      const copia = [...lista];
      copia[indiceExistente] = { ...copia[indiceExistente], cantidad: copia[indiceExistente].cantidad + cantidad };
      this.carrito.set(copia);
    } else {
      this.carrito.set([
        ...lista,
        { lineId: idUnico(), producto, variante, adicionales, cantidad },
      ]);
    }

    this.cerrarModalAgregar();
  }

  quitar(lineId: string): void {
    const lista = this.carrito();
    const indice = lista.findIndex((i) => i.lineId === lineId);
    if (indice < 0) return;

    const existente = lista[indice];
    if (existente.cantidad <= 1) {
      this.carrito.set(lista.filter((i) => i.lineId !== lineId));
    } else {
      const copia = [...lista];
      copia[indice] = { ...existente, cantidad: existente.cantidad - 1 };
      this.carrito.set(copia);
    }
  }

  sumarUno(lineId: string): void {
    const lista = this.carrito();
    const indice = lista.findIndex((i) => i.lineId === lineId);
    if (indice < 0) return;

    const copia = [...lista];
    copia[indice] = { ...copia[indice], cantidad: copia[indice].cantidad + 1 };
    this.carrito.set(copia);
  }

  vaciarCarrito(): void {
    this.carrito.set([]);
  }

  confirmarVenta(): void {
    if (this.itemsCarrito().length === 0) return;

    if (!this.cajaAbierta()) {
      this.toast.error('Abrí la caja antes de registrar ventas');
      return;
    }

    this.procesando.set(true);

    const request = {
      metodoPago: this.metodoPago(),
      items: this.itemsCarrito().map((item) => ({
        productoId: item.producto.id!,
        varianteId: item.variante?.id,
        adicionalIds: item.adicionales.map((a) => a.adicionalId),
        cantidad: item.cantidad,
      })),
    };

    this.ventaService.crear(request).subscribe({
      next: (venta) => {
        this.procesando.set(false);
        this.carrito.set([]);
        this.montoRecibido.set(0);
        this.ultimaVenta.set(venta);
        this.mostrarTicket.set(true);
        this.cargarProductos();
      },
      error: (err) => {
        this.procesando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo registrar la venta');
      },
    });
  }

  cerrarTicket(): void {
    this.mostrarTicket.set(false);
  }
}
