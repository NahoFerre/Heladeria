import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoService } from '../../services/producto';
import { InsumoService } from '../../services/insumo';
import {
  Adicional,
  ConfigurarRecetaRequest,
  GrupoAdicionales,
  MovimientoInventario,
  Producto,
  ProductoVariante,
  TipoMovimiento,
} from '../../models/producto.model';
import { Insumo } from '../../models/insumo.model';
import { formatearUnidadMedida } from '../../shared/unidad-medida';
import { paginar, totalPaginas } from '../../shared/pagination';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

interface ItemRecetaForm {
  insumoId: number;
  cantidad: number;
}

interface RecetaVarianteForm {
  varianteId: number;
  varianteNombre: string;
  items: ItemRecetaForm[];
}

const PRODUCTO_VACIO: Producto = {
  nombre: '',
  categoria: '',
  costoUnitario: 0,
  precioVenta: 0,
  unidadMedida: 'unidad',
  stockActual: 0,
  stockMinimo: 0,
  activo: true,
};

type Columna = 'nombre' | 'categoria' | 'costoUnitario' | 'precioVenta' | 'stockActual';
const TAMANO_PAGINA = 8;

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe, Spinner, Icon, Paginacion],
  selector: 'app-inventario',
  styleUrl: './inventario.scss',
  templateUrl: './inventario.html',
})
export class Inventario implements OnInit {
  private productoService = inject(ProductoService);
  private insumoService = inject(InsumoService);
  private toast = inject(ToastService);

  productos = signal<Producto[]>([]);
  categorias = signal<string[]>([]);
  mostrarInactivos = signal(false);
  cargando = signal(false);

  busqueda = signal('');
  categoriaFiltro = signal('');
  columnaOrden = signal<Columna>('nombre');
  direccionOrden = signal<'asc' | 'desc'>('asc');
  pagina = signal(1);

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

  mostrarFormulario = signal(false);
  editando = signal<Producto | null>(null);
  formulario = signal<Producto>({ ...PRODUCTO_VACIO });

  productoMovimientos = signal<Producto | null>(null);
  movimientos = signal<MovimientoInventario[]>([]);
  nuevoMovimiento = signal<{ tipo: TipoMovimiento; cantidad: number; motivo: string }>({
    tipo: 'ENTRADA',
    cantidad: 0,
    motivo: '',
  });

  paginaMovimientos = signal(1);
  totalPaginasMovimientos = computed(() => totalPaginas(this.movimientos().length, TAMANO_PAGINA));
  movimientosPagina = computed(() => paginar(this.movimientos(), this.paginaMovimientos(), TAMANO_PAGINA));

  ngOnInit(): void {
    this.cargar();
    this.productoService.categorias().subscribe((categorias) => this.categorias.set(categorias));
    this.insumoService.listar().subscribe((insumos) => this.insumosDisponibles.set(insumos));
  }

  cargar(): void {
    this.cargando.set(true);
    this.productoService.listar(!this.mostrarInactivos()).subscribe({
      next: (productos) => {
        this.cargando.set(false);
        this.productos.set(productos);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudieron cargar los productos');
      },
    });
  }

  toggleInactivos(): void {
    this.mostrarInactivos.set(!this.mostrarInactivos());
    this.cargar();
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

  nuevoProducto(): void {
    this.editando.set(null);
    this.formulario.set({ ...PRODUCTO_VACIO });
    this.mostrarFormulario.set(true);
  }

  editar(producto: Producto): void {
    this.editando.set(producto);
    this.formulario.set({ ...producto });
    this.mostrarFormulario.set(true);
  }

  cancelarEdicion(): void {
    this.editando.set(null);
    this.formulario.set({ ...PRODUCTO_VACIO });
    this.mostrarFormulario.set(false);
  }

  guardar(): void {
    const datos = this.formulario();
    const actual = this.editando();
    const esEdicion = !!actual?.id;

    const obs = esEdicion ? this.productoService.actualizar(actual!.id!, datos) : this.productoService.crear(datos);

    obs.subscribe({
      next: () => {
        this.cancelarEdicion();
        this.cargar();
        this.toast.exito(esEdicion ? 'Producto actualizado correctamente' : 'Producto creado correctamente');
      },
      error: () => this.toast.error('No se pudo guardar el producto. Revisá los datos.'),
    });
  }

  desactivar(producto: Producto): void {
    if (!producto.id) return;
    this.productoService.desactivar(producto.id).subscribe({
      next: () => {
        this.cargar();
        this.toast.exito(`Producto "${producto.nombre}" dado de baja`);
      },
      error: () => this.toast.error('No se pudo dar de baja el producto'),
    });
  }

  verMovimientos(producto: Producto): void {
    this.productoMovimientos.set(producto);
    this.nuevoMovimiento.set({ tipo: 'ENTRADA', cantidad: 0, motivo: '' });
    this.paginaMovimientos.set(1);
    this.productoService.listarMovimientos(producto.id!).subscribe((movs) => this.movimientos.set(movs));
  }

  cerrarMovimientos(): void {
    this.productoMovimientos.set(null);
    this.movimientos.set([]);
  }

  paginaAnteriorMovimientos(): void {
    this.paginaMovimientos.update((p) => Math.max(1, p - 1));
  }

  paginaSiguienteMovimientos(): void {
    this.paginaMovimientos.update((p) => Math.min(this.totalPaginasMovimientos(), p + 1));
  }

  registrarMovimiento(): void {
    const producto = this.productoMovimientos();
    if (!producto?.id) return;

    this.productoService.registrarMovimiento(producto.id, this.nuevoMovimiento()).subscribe({
      next: (movimiento) => {
        // La respuesta trae el producto ya actualizado; evita mostrar el stock viejo en el modal
        this.verMovimientos(movimiento.producto ?? producto);
        this.cargar();
        this.toast.exito('Movimiento registrado correctamente');
      },
      error: (err) => this.toast.error(err.error?.error ?? 'No se pudo registrar el movimiento'),
    });
  }

  // Variantes y grupos de adicionales ("constructor de productos")
  productoOpciones = signal<Producto | null>(null);
  variantesForm = signal<ProductoVariante[]>([]);
  gruposForm = signal<GrupoAdicionales[]>([]);
  guardandoOpciones = signal(false);

  abrirOpciones(producto: Producto): void {
    this.productoOpciones.set(producto);
    this.variantesForm.set((producto.variantes ?? []).map((v) => ({ ...v })));
    this.gruposForm.set((producto.gruposAdicionales ?? []).map((g) => ({ ...g, opciones: g.opciones.map((o) => ({ ...o })) })));
  }

  cerrarOpciones(): void {
    this.productoOpciones.set(null);
    this.variantesForm.set([]);
    this.gruposForm.set([]);
  }

  agregarVariante(): void {
    this.variantesForm.update((lista) => [
      ...lista,
      { nombre: '', precio: 0, costo: 0, stockActual: 0, stockMinimo: 0, activo: true },
    ]);
  }

  quitarVariante(indice: number): void {
    this.variantesForm.update((lista) => lista.filter((_, i) => i !== indice));
  }

  agregarGrupo(): void {
    this.gruposForm.update((lista) => [...lista, { nombre: '', minSeleccion: 0, maxSeleccion: 1, opciones: [] }]);
  }

  quitarGrupo(indice: number): void {
    this.gruposForm.update((lista) => lista.filter((_, i) => i !== indice));
  }

  agregarOpcion(grupoIndice: number): void {
    this.gruposForm.update((lista) => {
      const copia = [...lista];
      copia[grupoIndice] = {
        ...copia[grupoIndice],
        opciones: [
          ...copia[grupoIndice].opciones,
          { nombre: '', precioAdicional: 0, costoAdicional: 0, controlaStock: false, stockActual: 0, stockMinimo: 0, activo: true },
        ],
      };
      return copia;
    });
  }

  quitarOpcion(grupoIndice: number, opcionIndice: number): void {
    this.gruposForm.update((lista) => {
      const copia = [...lista];
      copia[grupoIndice] = {
        ...copia[grupoIndice],
        opciones: copia[grupoIndice].opciones.filter((_: Adicional, i: number) => i !== opcionIndice),
      };
      return copia;
    });
  }

  guardarOpciones(): void {
    const producto = this.productoOpciones();
    if (!producto?.id) return;

    this.guardandoOpciones.set(true);
    this.productoService
      .configurarOpciones(producto.id, { variantes: this.variantesForm(), gruposAdicionales: this.gruposForm() })
      .subscribe({
        next: () => {
          this.guardandoOpciones.set(false);
          this.cerrarOpciones();
          this.cargar();
          this.toast.exito('Configuración guardada correctamente');
        },
        error: (err) => {
          this.guardandoOpciones.set(false);
          this.toast.error(err.error?.error ?? 'No se pudo guardar la configuración');
        },
      });
  }

  // Recetas (Fase 2 del roadmap: insumos y recetas)
  insumosDisponibles = signal<Insumo[]>([]);
  productoReceta = signal<Producto | null>(null);
  recetaProductoForm = signal<ItemRecetaForm[]>([]);
  recetasVariantesForm = signal<RecetaVarianteForm[]>([]);
  guardandoReceta = signal(false);

  formatearUnidadMedida = formatearUnidadMedida;

  abrirReceta(producto: Producto): void {
    this.productoReceta.set(producto);
    if (!producto.variantes?.length) {
      this.recetaProductoForm.set((producto.receta ?? []).map((r) => ({ insumoId: r.insumo.id!, cantidad: r.cantidad })));
      this.recetasVariantesForm.set([]);
    } else {
      this.recetaProductoForm.set([]);
      this.recetasVariantesForm.set(
        producto.variantes.map((v) => ({
          varianteId: v.id!,
          varianteNombre: v.nombre,
          items: (v.receta ?? []).map((r) => ({ insumoId: r.insumo.id!, cantidad: r.cantidad })),
        })),
      );
    }
  }

  cerrarReceta(): void {
    this.productoReceta.set(null);
    this.recetaProductoForm.set([]);
    this.recetasVariantesForm.set([]);
  }

  agregarInsumoProducto(): void {
    const primerInsumo = this.insumosDisponibles()[0];
    if (!primerInsumo) return;
    this.recetaProductoForm.update((lista) => [...lista, { insumoId: primerInsumo.id!, cantidad: 0 }]);
  }

  quitarInsumoProducto(indice: number): void {
    this.recetaProductoForm.update((lista) => lista.filter((_, i) => i !== indice));
  }

  agregarInsumoVariante(varianteIndice: number): void {
    const primerInsumo = this.insumosDisponibles()[0];
    if (!primerInsumo) return;
    this.recetasVariantesForm.update((lista) => {
      const copia = [...lista];
      copia[varianteIndice] = {
        ...copia[varianteIndice],
        items: [...copia[varianteIndice].items, { insumoId: primerInsumo.id!, cantidad: 0 }],
      };
      return copia;
    });
  }

  quitarInsumoVariante(varianteIndice: number, itemIndice: number): void {
    this.recetasVariantesForm.update((lista) => {
      const copia = [...lista];
      copia[varianteIndice] = {
        ...copia[varianteIndice],
        items: copia[varianteIndice].items.filter((_, i) => i !== itemIndice),
      };
      return copia;
    });
  }

  guardarReceta(): void {
    const producto = this.productoReceta();
    if (!producto?.id) return;

    this.guardandoReceta.set(true);
    const request: ConfigurarRecetaRequest = {
      recetaProducto: this.recetaProductoForm().filter((i) => i.insumoId && i.cantidad > 0),
      recetasVariantes: this.recetasVariantesForm().map((rv) => ({
        varianteId: rv.varianteId,
        items: rv.items.filter((i) => i.insumoId && i.cantidad > 0),
      })),
    };

    this.productoService.configurarReceta(producto.id, request).subscribe({
      next: () => {
        this.guardandoReceta.set(false);
        this.cerrarReceta();
        this.cargar();
        this.toast.exito('Receta guardada correctamente');
      },
      error: (err) => {
        this.guardandoReceta.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo guardar la receta');
      },
    });
  }
}
