import { Component, Input } from '@angular/core';

const STYLES: Record<string, string> = {
  PENDING: 'bg-marigold/20 text-[#8a6600]',
  PENDING_VERIFICATION: 'bg-marigold/20 text-[#8a6600]',
  CONFIRMED: 'bg-savanna/15 text-savanna',
  APPROVED: 'bg-savanna/15 text-savanna',
  VERIFIED: 'bg-savanna/15 text-savanna',
  PAID: 'bg-savanna/15 text-savanna',
  ONGOING: 'bg-ink/10 text-ink',
  COMPLETED: 'bg-savanna text-white',
  CANCELLED: 'bg-brick/10 text-brick',
  REJECTED: 'bg-brick/10 text-brick',
  FAILED: 'bg-brick/10 text-brick',
  UNDER_MAINTENANCE: 'bg-charcoal/10 text-charcoal',
};

@Component({
  selector: 'app-status-pill',
  standalone: true,
  template: `
    @if (status) {
      <span class="inline-block px-2.5 py-1 rounded-sign text-xs font-semibold {{ styleFor() }}">
        {{ label() }}
      </span>
    }
  `,
})
export class StatusPillComponent {
  @Input() status: string | null | undefined = null;

  styleFor() {
    return (this.status && STYLES[this.status]) || 'bg-mist text-charcoal';
  }

  label() {
    return this.status ? this.status.replaceAll('_', ' ') : '';
  }
}
