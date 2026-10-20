import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditoriaService } from '../../services/auditoria';
import { UsuarioService } from '../../services/usuario';
import { RegistroAuditoria } from '../../models/auditoria.model';
import { Usuario } from '../../models/usuario.model';
import { Spinner } from '../../shared/spinner/spinner';
import { Paginacion } from '../../shared/paginacion/paginacion';

const TAMANO_PAGINA = 50;

const ETIQUETAS_ACCION: Record<string, string> = {
  LOGIN: 'Inicio de sesión',
  VENTA_CREADA: 'Venta creada',
  VENTA_ANULADA: 'Venta anulada',
  PRODUCTO_CREADO: 'Producto creado',
  PRODUCTO_EDITADO: 'Producto editado',
  PRECIO_CAMBIADO: 'Cambio de precio',
  STOCK_AJUSTADO: 'Stock ajustado',
  CAJA_ABIERTA: 'Caja abierta',
  CAJA_CERRADA: 'Caja cerrada',
  RETIRO_EFECTIVO: 'Retiro de efectivo',
  INGRESO_EFECTIVO: 'Ingreso de efectivo',
  GASTO_REGISTRADO: 'Gasto registrado',
  USUARIO_CREADO: 'Usuario creado',
  USUARIO_ELIMINADO: 'Usuario eliminado',
  COMPRA_REGISTRADA: 'Compra registrada',
  MERMA_REGISTRADA: 'Pérdida registrada',
  PROMOCION_CREADA: 'Promoción creada',
  PROMOCION_EDITADA: 'Promoción editada',
  PROMOCION_ELIMINADA: 'Promoción eliminada',
};

@Component({
  imports: [DatePipe, FormsModule, Spinner, Paginacion],
  selector: 'app-auditoria',
  styleUrl: './auditoria.scss',
  templateUrl: './auditoria.html',
})
export class Auditoria implements OnInit {
  private auditoriaService = inject(AuditoriaService);
  private usuarioService = inject(UsuarioService);

  registros = signal<RegistroAuditoria[]>([]);
  usuarios = signal<Usuario[]>([]);
  cargando = signal(true);

  pagina = signal(1);
  totalPaginas = signal(1);

  desde = signal('');
  hasta = signal('');
  usuarioId = signal<number | null>(null);

  ngOnInit(): void {
    this.usuarioService.listar().subscribe((usuarios) => this.usuarios.set(usuarios));
    this.cargar();
  }

  aplicarFiltros(): void {
    this.pagina.set(1);
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.auditoriaService
      .listar(this.pagina() - 1, TAMANO_PAGINA, {
        desde: this.desde() || undefined,
        hasta: this.hasta() || undefined,
        usuarioId: this.usuarioId() ?? undefined,
      })
      .subscribe({
        next: (respuesta) => {
          this.cargando.set(false);
          this.registros.set(respuesta.content);
          this.totalPaginas.set(Math.max(1, respuesta.totalPages));
        },
        error: () => {
          this.cargando.set(false);
        },
      });
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
    this.cargar();
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
    this.cargar();
  }

  etiquetaAccion(accion: string): string {
    return ETIQUETAS_ACCION[accion] ?? accion;
  }
}
