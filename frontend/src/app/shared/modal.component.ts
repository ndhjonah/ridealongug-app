import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-modal',
  standalone: true,
  template: `
    @if (open) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-ink/60" (click)="close.emit()"></div>
        <div class="relative bg-paper rounded-sign max-w-lg w-full max-h-[85vh] overflow-y-auto p-6 shadow-xl">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-display text-lg font-semibold">{{ title }}</h3>
            <button (click)="close.emit()" class="text-charcoal hover:text-ink text-xl leading-none" aria-label="Close">
              &times;
            </button>
          </div>
          <ng-content></ng-content>
        </div>
      </div>
    }
  `,
})
export class ModalComponent {
  @Input() open = false;
  @Input() title = '';
  @Output() close = new EventEmitter<void>();
}
