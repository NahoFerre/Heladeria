import { Component, ElementRef, effect, input, output, signal, viewChild } from '@angular/core';

@Component({
  selector: 'app-monto-input',
  templateUrl: './monto-input.html',
})
export class MontoInput {
  value = input<number>(0);
  valueChange = output<number>();
  placeholder = input('0');

  private inputRef = viewChild<ElementRef<HTMLInputElement>>('inputEl');

  texto = signal('');

  constructor() {
    effect(() => {
      const valor = this.value();
      const elementoActivo = this.inputRef()?.nativeElement === document.activeElement;
      if (!elementoActivo) {
        this.texto.set(this.formatear(valor));
      }
    });
  }

  onFocus(): void {
    if (this.value() === 0) {
      this.texto.set('');
    }
    setTimeout(() => this.inputRef()?.nativeElement.select());
  }

  onInput(event: Event): void {
    const el = event.target as HTMLInputElement;
    const digitos = el.value.replace(/\D/g, '');
    const numero = digitos ? parseInt(digitos, 10) : 0;
    this.texto.set(digitos ? this.agruparMiles(digitos) : '');
    this.valueChange.emit(numero);
  }

  onBlur(): void {
    this.texto.set(this.formatear(this.value()));
  }

  private formatear(valor: number): string {
    return valor ? this.agruparMiles(String(valor)) : '';
  }

  private agruparMiles(digitos: string): string {
    return digitos.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  }
}
