import { Component, input, output } from '@angular/core';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-paginacion',
  imports: [Icon],
  styleUrl: './paginacion.scss',
  templateUrl: './paginacion.html',
})
export class Paginacion {
  pagina = input.required<number>();
  totalPaginas = input.required<number>();
  anterior = output<void>();
  siguiente = output<void>();
}
