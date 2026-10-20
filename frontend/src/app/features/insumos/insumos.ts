import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InsumoService } from '../../services/insumo';
import { Insumo, UnidadMedida } from '../../models/insumo.model';
import { formatearUnidadMedida } from '../../shared/unidad-medida';
import { paginar, totalPaginas } from '../../shared/pagination';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

const INSUMO_VACIO: Insumo = {
  nombre: '',
  unidadMedida: 'UNIDAD',
  costoUnitario: 0,
  stockActual: 0,
  stockMinimo: 0,
  proveedor: '',
  activo: true,
};

const TAMANO_PAGINA = 8;

@Component({
  imports: [FormsModule, CurrencyPipe, Spinner, Icon, Paginacion],
  selector: 'app-insumos',
  styleUrl: './insumos.scss',
  templateUrl: './insumos.html',
})
export class Insumos implements OnInit {
  private insumoService = inject(InsumoService);
  private toast = inject(ToastService);

  insumos = signal<Insumo[]>([]);
  mostrarInactivos = signal(false);
  cargando = signal(false);

  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.insumos().length, TAMANO_PAGINA));
  insumosPagina = computed(() => paginar(this.insumos(), this.pagina(), TAMANO_PAGINA));

  mostrarFormulario = signal(false);
  editando = signal<Insumo | null>(null);
  formulario = signal<Insumo>({ ...INSUMO_VACIO });

  formatearUnidadMedida = formatearUnidadMedida;
  unidades: UnidadMedida[] = ['UNIDAD', 'GRAMO', 'KILOGRAMO', 'MILILITRO', 'LITRO'];

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.insumoService.listar(!this.mostrarInactivos()).subscribe({
      next: (insumos) => {
        this.cargando.set(false);
        this.insumos.set(insumos);
        this.pagina.set(1);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudieron cargar los insumos');
      },
    });
  }

  toggleInactivos(): void {
    this.mostrarInactivos.set(!this.mostrarInactivos());
    this.cargar();
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  nuevoInsumo(): void {
    this.editando.set(null);
    this.formulario.set({ ...INSUMO_VACIO });
    this.mostrarFormulario.set(true);
  }

  editar(insumo: Insumo): void {
    this.editando.set(insumo);
    this.formulario.set({ ...insumo });
    this.mostrarFormulario.set(true);
  }

  cancelarEdicion(): void {
    this.editando.set(null);
    this.formulario.set({ ...INSUMO_VACIO });
    this.mostrarFormulario.set(false);
  }

  guardar(): void {
    const datos = this.formulario();
    const actual = this.editando();
    const esEdicion = !!actual?.id;

    const obs = esEdicion ? this.insumoService.actualizar(actual!.id!, datos) : this.insumoService.crear(datos);

    obs.subscribe({
      next: () => {
        this.cancelarEdicion();
        this.cargar();
        this.toast.exito(esEdicion ? 'Insumo actualizado correctamente' : 'Insumo creado correctamente');
      },
      error: () => this.toast.error('No se pudo guardar el insumo. Revisá los datos.'),
    });
  }

  desactivar(insumo: Insumo): void {
    if (!insumo.id) return;
    this.insumoService.desactivar(insumo.id).subscribe({
      next: () => {
        this.cargar();
        this.toast.exito(`Insumo "${insumo.nombre}" dado de baja`);
      },
      error: () => this.toast.error('No se pudo dar de baja el insumo'),
    });
  }
}
