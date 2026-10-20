import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GastoService } from '../../services/gasto';
import { Gasto, GastoRequest } from '../../models/gasto.model';
import { paginar, totalPaginas } from '../../shared/pagination';
import { ToastService } from '../../shared/toast/toast';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';
import { MontoInput } from '../../shared/monto-input/monto-input';

const TAMANO_PAGINA = 8;

const CATEGORIAS_SUGERIDAS = ['Limpieza', 'Transporte', 'Hielo', 'Servicios', 'Insumos administrativos', 'Mantenimiento', 'Delivery', 'Otros'];

interface ResumenFila {
  nombre: string;
  monto: number;
}

@Component({
  imports: [FormsModule, CurrencyPipe, DatePipe, Spinner, Icon, Paginacion, MontoInput],
  selector: 'app-gastos',
  styleUrl: './gastos.scss',
  templateUrl: './gastos.html',
})
export class Gastos implements OnInit {
  private gastoService = inject(GastoService);
  private toast = inject(ToastService);

  categoria = signal('');
  descripcion = signal('');
  monto = signal(0);
  metodoPago = signal('');
  observacion = signal('');
  procesando = signal(false);

  gastos = signal<Gasto[]>([]);
  cargando = signal(true);
  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.gastos().length, TAMANO_PAGINA));
  gastosPagina = computed(() => paginar(this.gastos(), this.pagina(), TAMANO_PAGINA));

  totalGastado = computed(() => this.gastos().reduce((acc, g) => acc + g.monto, 0));
  porCategoria = computed(() => {
    const mapa = new Map<string, number>();
    for (const gasto of this.gastos()) {
      const clave = gasto.categoria || 'Sin categoría';
      mapa.set(clave, (mapa.get(clave) ?? 0) + gasto.monto);
    }
    return [...mapa.entries()]
      .map(([nombre, monto]): ResumenFila => ({ nombre, monto }))
      .sort((a, b) => b.monto - a.monto);
  });

  categoriasSugeridas = CATEGORIAS_SUGERIDAS;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.gastoService.listar().subscribe({
      next: (gastos) => {
        this.cargando.set(false);
        this.gastos.set(gastos);
        this.pagina.set(1);
      },
      error: () => {
        this.cargando.set(false);
        this.toast.error('No se pudo cargar el historial de gastos');
      },
    });
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  registrarGasto(): void {
    if (!this.descripcion().trim()) {
      this.toast.error('La descripción es obligatoria');
      return;
    }
    if (this.monto() <= 0) {
      this.toast.error('El monto debe ser mayor a cero');
      return;
    }

    const request: GastoRequest = {
      categoria: this.categoria() || undefined,
      descripcion: this.descripcion(),
      monto: this.monto(),
      metodoPago: this.metodoPago() || undefined,
      observacion: this.observacion() || undefined,
    };

    this.procesando.set(true);
    this.gastoService.crear(request).subscribe({
      next: () => {
        this.procesando.set(false);
        this.limpiarFormulario();
        this.cargar();
        this.toast.exito('Gasto registrado correctamente');
      },
      error: (err) => {
        this.procesando.set(false);
        this.toast.error(err.error?.error ?? 'No se pudo registrar el gasto');
      },
    });
  }

  private limpiarFormulario(): void {
    this.categoria.set('');
    this.descripcion.set('');
    this.monto.set(0);
    this.metodoPago.set('');
    this.observacion.set('');
  }
}
