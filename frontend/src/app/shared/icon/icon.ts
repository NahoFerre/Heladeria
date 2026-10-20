import { Component, input } from '@angular/core';

export type NombreIcono =
  | 'home'
  | 'cart'
  | 'wallet'
  | 'box'
  | 'trending-up'
  | 'archive'
  | 'users'
  | 'log-out'
  | 'check-circle'
  | 'alert-triangle'
  | 'inbox'
  | 'bar-chart'
  | 'chevron-left'
  | 'chevron-right'
  | 'printer'
  | 'eye'
  | 'file-text'
  | 'layers'
  | 'ban'
  | 'trending-down'
  | 'shield'
  | 'trash'
  | 'credit-card'
  | 'settings';

@Component({
  selector: 'app-icon',
  templateUrl: './icon.html',
})
export class Icon {
  name = input.required<NombreIcono>();
  size = input(20);
}
