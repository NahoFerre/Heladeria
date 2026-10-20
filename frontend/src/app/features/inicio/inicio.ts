import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe, LowerCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReporteService } from '../../services/reporte';
import { CajaService } from '../../services/caja';
import { Auth } from '../../core/auth';
import { Dashboard as DashboardResumen, ResumenHoy } from '../../models/reporte.model';
import { Producto } from '../../models/producto.model';
import { CajaActual } from '../../models/caja.model';
import { fechaLocalIso } from '../../shared/fecha';
import { formatearUnidadMedida } from '../../shared/unidad-medida';
import { Spinner } from '../../shared/spinner/spinner';
import { Icon } from '../../shared/icon/icon';

@Component({
  imports: [CurrencyPipe, DatePipe, DecimalPipe, LowerCasePipe, RouterLink, Spinner, Icon],
  selector: 'app-inicio',
  styleUrl: './inicio.scss',
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  private reporteService = inject(ReporteService);
  private cajaService = inject(CajaService);
  private auth = inject(Auth);

  usuario = this.auth.getUsuario();
  esAdmin = this.auth.esAdmin();
  hoy = new Date();

  resumen = signal<ResumenHoy | null>(null);
  stockBajo = signal<Producto[]>([]);
  gananciaHoy = signal<number | null>(null);
  cajaActual = signal<CajaActual | null>(null);
  cargando = signal(true);

  dashboard = signal<DashboardResumen | null>(null);
  cargandoDashboard = signal(false);
  formatearUnidadMedida = formatearUnidadMedida;

  ngOnInit(): void {
    let pendientes = this.esAdmin ? 4 : 3;
    const marcarListo = () => {
      pendientes--;
      if (pendientes === 0) this.cargando.set(false);
    };

    this.reporteService.resumenHoy().subscribe((r) => {
      this.resumen.set(r);
      marcarListo();
    });
    this.reporteService.stockBajo().subscribe((s) => {
      this.stockBajo.set(s);
      marcarListo();
    });
    this.cajaService.actual().subscribe((c) => {
      this.cajaActual.set(c);
      marcarListo();
    });

    if (this.esAdmin) {
      const hoy = fechaLocalIso();
      this.reporteService.ganancias(hoy, hoy).subscribe((g) => {
        this.gananciaHoy.set(g.ganancia);
        marcarListo();
      });

      this.cargandoDashboard.set(true);
      this.reporteService.dashboard().subscribe((d) => {
        this.dashboard.set(d);
        this.cargandoDashboard.set(false);
      });
    }
  }
}
