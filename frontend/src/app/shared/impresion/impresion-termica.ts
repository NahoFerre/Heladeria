import { Service } from '@angular/core';
import * as qz from 'qz-tray';
import { Venta } from '../../models/venta.model';
import { construirTicketEscPos } from './escpos-ticket';

const STORAGE_KEY = 'heladeria_impresora_termica';

// Envuelve QZ Tray para imprimir el ticket como comandos ESC/POS crudos, sin pasar por el
// diálogo de impresión del navegador. Es un ajuste por máquina (cada caja tiene su propia
// impresora física conectada), por eso el nombre elegido se guarda en localStorage y no en el
// backend: no tiene sentido que viaje entre cajas ni que dependa del usuario que inició sesión.
@Service()
export class ImpresionTermica {
  private conexion: Promise<void> | null = null;

  impresoraConfigurada(): string | null {
    return localStorage.getItem(STORAGE_KEY);
  }

  configurarImpresora(nombre: string | null): void {
    if (nombre) {
      localStorage.setItem(STORAGE_KEY, nombre);
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  async listarImpresoras(): Promise<string[]> {
    await this.conectar();
    return qz.printers.find();
  }

  // Devuelve true si el ticket salió por la térmica, false si no hay impresora configurada o no
  // se pudo conectar con QZ Tray — en ese caso el llamador decide si usa window.print() como
  // respaldo (ver Ticket.imprimir()).
  async imprimirTicket(venta: Venta): Promise<boolean> {
    const impresora = this.impresoraConfigurada();
    if (!impresora) return false;

    try {
      await this.conectar();
      const config = qz.configs.create(impresora, { encoding: 'ISO-8859-1' });
      await qz.print(config, construirTicketEscPos(venta));
      return true;
    } catch (error) {
      console.error(
        'No se pudo imprimir con QZ Tray, se usará la impresión del navegador como respaldo.',
        error,
      );
      return false;
    }
  }

  private conectar(): Promise<void> {
    if (qz.websocket.isActive()) return Promise.resolve();
    if (!this.conexion) {
      this.conexion = qz.websocket.connect().catch((error: unknown) => {
        this.conexion = null;
        throw error;
      });
    }
    return this.conexion;
  }
}
