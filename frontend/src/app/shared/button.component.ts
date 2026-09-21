import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled"
      (click)="clicked.emit($event)"
      [ngClass]="[base, variants[variant], fullWidth ? 'w-full' : '', className]"
    >
      <ng-content></ng-content>
    </button>
  `,
})
export class ButtonComponent {
  @Input() variant: 'primary' | 'accent' | 'outline' | 'ghost' | 'danger' = 'primary';
  @Input() type: 'button' | 'submit' = 'button';
  @Input() disabled = false;
  @Input() fullWidth = false;
  @Input() className = '';
  @Output() clicked = new EventEmitter<Event>();

  base = 'inline-flex items-center justify-center px-5 py-2.5 rounded-sign font-semibold text-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed';

  variants: Record<string, string> = {
    primary: 'bg-ink text-paper hover:bg-charcoal',
    accent: 'bg-marigold text-ink hover:brightness-95',
    outline: 'border border-ink text-ink hover:bg-ink hover:text-paper',
    ghost: 'text-ink hover:bg-mist',
    danger: 'bg-brick text-paper hover:brightness-90',
  };
}
