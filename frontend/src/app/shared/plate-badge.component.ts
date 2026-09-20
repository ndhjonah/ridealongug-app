import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-plate-badge',
  standalone: true,
  template: `
    <span class="inline-block font-plate text-xs tracking-wide bg-ink text-marigold px-2 py-1 rounded-sign">
      {{ plate }}
    </span>
  `,
})
export class PlateBadgeComponent {
  @Input() plate = '';
}
