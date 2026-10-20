import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import { ConfigurarOpcionesRequest, ConfigurarRecetaRequest, MovimientoInventario, Producto } from '../models/producto.model';

@Service()
export class ProductoService {
  private http = inject(HttpClient);

  listar(soloActivos = true): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${API_URL}/productos`, { params: { soloActivos } });
  }

  categorias(): Observable<string[]> {
    return this.http.get<string[]>(`${API_URL}/productos/categorias`);
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${API_URL}/productos/${id}`);
  }

  crear(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(`${API_URL}/productos`, producto);
  }

  actualizar(id: number, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${API_URL}/productos/${id}`, producto);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/productos/${id}`);
  }

  listarMovimientos(productoId: number): Observable<MovimientoInventario[]> {
    return this.http.get<MovimientoInventario[]>(`${API_URL}/productos/${productoId}/movimientos`);
  }

  registrarMovimiento(productoId: number, movimiento: MovimientoInventario): Observable<MovimientoInventario> {
    return this.http.post<MovimientoInventario>(`${API_URL}/productos/${productoId}/movimientos`, movimiento);
  }

  configurarOpciones(productoId: number, request: ConfigurarOpcionesRequest): Observable<Producto> {
    return this.http.put<Producto>(`${API_URL}/productos/${productoId}/opciones`, request);
  }

  configurarReceta(productoId: number, request: ConfigurarRecetaRequest): Observable<Producto> {
    return this.http.put<Producto>(`${API_URL}/productos/${productoId}/receta`, request);
  }
}
