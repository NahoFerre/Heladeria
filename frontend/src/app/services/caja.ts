import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../core/api-config';
import {
  AbrirCajaRequest,
  CajaActual,
  CajaHistorial,
  CerrarCajaRequest,
  CierreCaja,
  MovimientoCaja,
  MovimientoCajaRequest,
} from '../models/caja.model';

@Service()
export class CajaService {
  private http = inject(HttpClient);

  actual(): Observable<CajaActual | null> {
    return this.http.get<CajaActual | null>(`${API_URL}/cajas/actual`);
  }

  abrir(request: AbrirCajaRequest): Observable<CajaActual> {
    return this.http.post<CajaActual>(`${API_URL}/cajas/abrir`, request);
  }

  cerrar(request: CerrarCajaRequest): Observable<CierreCaja> {
    return this.http.post<CierreCaja>(`${API_URL}/cajas/cerrar`, request);
  }

  historial(): Observable<CajaHistorial[]> {
    return this.http.get<CajaHistorial[]>(`${API_URL}/cajas`);
  }

  registrarMovimiento(request: MovimientoCajaRequest): Observable<MovimientoCaja> {
    return this.http.post<MovimientoCaja>(`${API_URL}/cajas/movimientos`, request);
  }

  movimientosActual(): Observable<MovimientoCaja[]> {
    return this.http.get<MovimientoCaja[]>(`${API_URL}/cajas/movimientos/actual`);
  }

  movimientos(cajaId: number): Observable<MovimientoCaja[]> {
    return this.http.get<MovimientoCaja[]>(`${API_URL}/cajas/${cajaId}/movimientos`);
  }
}
