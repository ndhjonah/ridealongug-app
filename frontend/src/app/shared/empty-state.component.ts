import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="text-center py-16 px-6">
      <h3 class="font-display text-xl font-semibold text-ink mb-2">{{ title }}</h3>
      @if (description) {
        <p class="text-charcoal max-w-md mx-auto mb-6">{{ description }}</p>
      }
      <ng-content></ng-content>
    </div>
  `,
})
export class EmptyStateComponent {
  @Input() title = '';
  @Input() description = '';
}
