import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Auth } from '../../core/auth';
import { ToastContainer } from '../../shared/toast/toast-container';
import { Icon } from '../../shared/icon/icon';

const COLAPSADO_KEY = 'heladeria_sidebar_colapsado';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastContainer, Icon],
  selector: 'app-shell',
  styleUrl: './shell.scss',
  templateUrl: './shell.html',
})
export class Shell {
  private auth = inject(Auth);
  private router = inject(Router);

  usuario = this.auth.getUsuario();
  esAdmin = this.auth.esAdmin();

  colapsado = signal(localStorage.getItem(COLAPSADO_KEY) === 'true');

  toggleSidebar(): void {
    this.colapsado.update((v) => !v);
    localStorage.setItem(COLAPSADO_KEY, String(this.colapsado()));
  }

  cerrarSesion(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
