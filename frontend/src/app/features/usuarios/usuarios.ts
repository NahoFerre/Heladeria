import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../services/usuario';
import { Auth } from '../../core/auth';
import { Rol, Usuario } from '../../models/usuario.model';
import { ToastService } from '../../shared/toast/toast';
import { paginar, totalPaginas } from '../../shared/pagination';
import { Icon } from '../../shared/icon/icon';
import { Paginacion } from '../../shared/paginacion/paginacion';

const TAMANO_PAGINA = 8;

@Component({
  imports: [FormsModule, Icon, Paginacion],
  selector: 'app-usuarios',
  styleUrl: './usuarios.scss',
  templateUrl: './usuarios.html',
})
export class Usuarios implements OnInit {
  private usuarioService = inject(UsuarioService);
  private auth = inject(Auth);
  private toast = inject(ToastService);

  usernameActual = this.auth.getUsuario()?.username;

  usuarios = signal<Usuario[]>([]);
  pagina = signal(1);
  totalPaginas = computed(() => totalPaginas(this.usuarios().length, TAMANO_PAGINA));
  usuariosPagina = computed(() => paginar(this.usuarios(), this.pagina(), TAMANO_PAGINA));

  nuevoUsername = signal('');
  nuevoPassword = signal('');
  nuevoRol = signal<Rol>('CAJERO');

  procesando = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.usuarioService.listar().subscribe((usuarios) => {
      this.usuarios.set(usuarios);
      this.pagina.set(1);
    });
  }

  paginaAnterior(): void {
    this.pagina.update((p) => Math.max(1, p - 1));
  }

  paginaSiguiente(): void {
    this.pagina.update((p) => Math.min(this.totalPaginas(), p + 1));
  }

  crear(): void {
    this.procesando.set(true);

    this.usuarioService
      .crear({ username: this.nuevoUsername(), password: this.nuevoPassword(), rol: this.nuevoRol() })
      .subscribe({
        next: (usuario) => {
          this.procesando.set(false);
          this.toast.exito(`Usuario "${usuario.username}" creado correctamente`);
          this.nuevoUsername.set('');
          this.nuevoPassword.set('');
          this.nuevoRol.set('CAJERO');
          this.cargar();
        },
        error: (err) => {
          this.procesando.set(false);
          this.toast.error(err.error?.error ?? 'No se pudo crear el usuario');
        },
      });
  }

  eliminar(usuario: Usuario): void {
    if (!confirm(`¿Eliminar al usuario "${usuario.username}"? Esta acción no se puede deshacer.`)) {
      return;
    }
    this.usuarioService.eliminar(usuario.id).subscribe({
      next: () => {
        this.cargar();
        this.toast.exito(`Usuario "${usuario.username}" eliminado`);
      },
      error: (err) => this.toast.error(err.error?.error ?? 'No se pudo eliminar el usuario'),
    });
  }
}
