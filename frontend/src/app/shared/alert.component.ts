import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (message) {
      <div class="border rounded-sign px-4 py-3 text-sm" [ngClass]="styleFor(type)" role="status">
        {{ message }}
      </div>
    }
  `,
})
export class AlertComponent {
  @Input() message = '';
  @Input() type: 'error' | 'success' | 'info' = 'error';

  styleFor(type: string) {
    const styles: Record<string, string> = {
      error: 'bg-brick/10 text-brick border-brick/20',
      success: 'bg-savanna/10 text-savanna border-savanna/20',
      info: 'bg-mist text-charcoal border-mist',
    };
    return styles[type];
  }
}
