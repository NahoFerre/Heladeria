import { Routes } from '@angular/router';
import { Login } from './features/login/login';
import { Shell } from './layout/shell/shell';
import { authGuard } from './core/auth-guard';
import { adminGuard } from './core/admin-guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio) },
      { path: 'venta', loadComponent: () => import('./features/venta/venta').then((m) => m.Venta) },
      { path: 'caja', loadComponent: () => import('./features/caja/caja').then((m) => m.Caja) },
      {
        path: 'registro-ventas',
        loadComponent: () => import('./features/registro-ventas/registro-ventas').then((m) => m.RegistroVentas),
      },
      {
        path: 'inventario',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/inventario/inventario').then((m) => m.Inventario),
      },
      {
        path: 'insumos',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/insumos/insumos').then((m) => m.Insumos),
      },
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'reportes',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: 'historial-cajas',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/historial-cajas/historial-cajas').then((m) => m.HistorialCajas),
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/usuarios/usuarios').then((m) => m.Usuarios),
      },
      {
        path: 'auditoria',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/auditoria/auditoria').then((m) => m.Auditoria),
      },
      {
        path: 'compras',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/compras/compras').then((m) => m.Compras),
      },
      {
        path: 'perdidas',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/mermas/mermas').then((m) => m.Mermas),
      },
      {
        path: 'gastos',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/gastos/gastos').then((m) => m.Gastos),
      },
      {
        path: 'promociones',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/promociones/promociones').then((m) => m.Promociones),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
