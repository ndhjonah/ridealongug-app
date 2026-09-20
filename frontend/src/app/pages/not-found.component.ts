import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonComponent } from '../shared/button.component';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, ButtonComponent],
  template: `
    <div class="max-w-md mx-auto px-6 py-24 text-center">
      <h1 class="font-display text-3xl font-bold mb-3">Wrong turn</h1>
      <p class="text-charcoal mb-6">That page doesn't exist. Let's get you back on the road.</p>
      <a routerLink="/">
        <app-button variant="accent">Back to browsing</app-button>
      </a>
    </div>
  `,
})
export class NotFoundComponent {}
