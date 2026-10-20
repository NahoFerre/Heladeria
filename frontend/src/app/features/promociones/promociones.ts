import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PromocionService } from '../../services/promocion';
import { ProductoService } from '../../services/producto';
import { Promocion, PromocionRequest, TipoPromocion } from '../../models/promocion.model';
import { Producto } from '../../models/producto.model';
import { paginar, totalPaginas } from '../../shared/pagination';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

interface FormularioPromocion {
  nombre: string;
  tipo: TipoPromocion;
  valor: number;
  productoId: number | null;
  categoria: string;
  fechaInicio: string;
  fechaFin: string;
  cantidadMinima: number;
  activo: boolean;
}

const FORMULARIO_VACIO: FormularioPromocion = {
  nombre: '',
  tipo: 'PORCENTAJE',
  valor: 0,
  productoId: null,
  categoria: '',
  fechaInicio: '',
  fechaFin: '',
  cantidadMinima: 1,
  activo: true,
};

const ETIQUETAS_TIPO: Record<TipoPromocion, string> = {
  PORCENTAJE: '% de descuento',
  MONTO_FIJO: 'Monto fijo de descuento',
  PRECIO_ESPECIAL: 'Precio especial',
};

const TAMANO_PAGINA = 8;

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe, Spinner, Icon, Paginacion],
  selector: 'app-promociones',
  styleUrl: './promociones.scss',
  templateUrl: './promociones.html',
})
export class Promociones implements OnInit {
  private promocionService = inject(PromocionService);
  private productoService = inject(ProductoService);
  private toast = inject(ToastService);

  promociones = signal<Promocion[]>([]);
  productos = signal<Producto[]>([]);
  mostrarInactivas = signal(false);
  cargando = signal(false);

  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.promociones().length, TAMANO_PAGINA));
  promocionesPagina = computed(() => paginar(this.promociones(), this.pagina(), TAMANO_PAGINA));

  mostrarFormulario = signal(false);
  editando = signal<Promocion | null>(null);
  formulario = signal<FormularioPromocion>({ ...FORMULARIO_VACIO });

  tipos: TipoPromocion[] = ['PORCENTAJE', 'MONTO_FIJO', 'PRECIO_ESPECIAL'];
  etiquetaTipo(tipo: TipoPromocion): string {
    return ETIQUETAS_TIPO[tipo];
  }

  ngOnInit(): void {
    this.productoService.listar(true).subscribe((productos) => this.productos.set(productos));
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.promocionService.listar(!this.mostrarInactivas()).subscribe({
      next: (promociones) => {
        this.cargando.set(false);
        this.promociones.set(promociones);
        this.pagina.set(1);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudieron cargar las promociones');
      },
    });
  }

  toggleInactivas(): void {
    this.mostrarInactivas.set(!this.mostrarInactivas());
    this.cargar();
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  nuevaPromocion(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.mostrarFormulario.set(true);
  }

  editar(promocion: Promocion): void {
    this.editando.set(promocion);
    this.formulario.set({
      nombre: promocion.nombre,
      tipo: promocion.tipo,
      valor: promocion.valor,
      productoId: promocion.producto?.id ?? null,
      categoria: promocion.categoria ?? '',
      fechaInicio: promocion.fechaInicio ?? '',
      fechaFin: promocion.fechaFin ?? '',
      cantidadMinima: promocion.cantidadMinima,
      activo: promocion.activo,
    });
    this.mostrarFormulario.set(true);
  }

  cancelarEdicion(): void {
    this.editando.set(null);
    this.formulario.set({ ...FORMULARIO_VACIO });
    this.mostrarFormulario.set(false);
  }

  guardar(): void {
    const datos = this.formulario();
    const actual = this.editando();
    const esEdicion = !!actual?.id;

    const request: PromocionRequest = {
      nombre: datos.nombre,
      tipo: datos.tipo,
      valor: datos.valor,
      productoId: datos.productoId,
      categoria: datos.categoria || null,
      fechaInicio: datos.fechaInicio || null,
      fechaFin: datos.fechaFin || null,
      cantidadMinima: datos.cantidadMinima,
      activo: datos.activo,
    };

    const obs = esEdicion ? this.promocionService.actualizar(actual!.id, request) : this.promocionService.crear(request);

    obs.subscribe({
      next: () => {
        this.cancelarEdicion();
        this.cargar();
        this.toast.exito(esEdicion ? 'Promoción actualizada correctamente' : 'Promoción creada correctamente');
      },
      error: () => this.toast.error('No se pudo guardar la promoción. Revisá los datos.'),
    });
  }

  eliminar(promocion: Promocion): void {
    this.promocionService.eliminar(promocion.id).subscribe({
      next: () => {
        this.cargar();
        this.toast.exito(`Promoción "${promocion.nombre}" dada de baja`);
      },
      error: () => this.toast.error('No se pudo dar de baja la promoción'),
    });
  }

  descripcionCondicion(promocion: Promocion): string {
    if (promocion.producto) return promocion.producto.nombre;
    if (promocion.categoria) return `Categoría: ${promocion.categoria}`;
    return 'Todo el catálogo';
  }
}
