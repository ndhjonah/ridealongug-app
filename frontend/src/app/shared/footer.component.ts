import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `
    <footer class="border-t border-mist mt-24">
      <div class="max-w-6xl mx-auto px-6 py-10 flex flex-col sm:flex-row items-center justify-between gap-4">
        <p class="font-display font-semibold text-ink">
          RideAlong<span class="text-marigold">UG</span>
        </p>
        <p class="text-sm text-charcoal">Cars, trucks and tractors, verified before every rental.</p>
      </div>
    </footer>
  `,
})
export class FooterComponent {}
