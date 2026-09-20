import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ButtonComponent } from './button.component';
import { NotificationBellComponent } from './notification-bell.component';

const ROLE_LINKS: Record<string, { to: string; label: string }[]> = {
  CUSTOMER: [
    { to: '/my-bookings', label: 'My bookings' },
    { to: '/my-licence', label: 'My licence' },
  ],
  VEHICLE_OWNER: [
    { to: '/owner/vehicles', label: 'My vehicles' },
    { to: '/owner/pickups', label: 'Pending pickups' },
    { to: '/owner/earnings', label: 'My earnings' },
    { to: '/owner/dealer', label: 'Business profile' },
  ],
  DRIVER: [{ to: '/driver', label: 'Driver dashboard' }],
  ADMINISTRATOR: [{ to: '/admin', label: 'Admin' }],
};

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonComponent, NotificationBellComponent],
  template: `
    <header class="border-b border-mist bg-paper sticky top-0 z-30">
      <div class="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <a routerLink="/" class="font-display font-bold text-lg tracking-tight text-ink">
          RideAlong<span class="text-marigold">UG</span>
        </a>

        <nav class="hidden md:flex items-center gap-6">
          @if (user()) {
            <a routerLink="/profile" class="text-sm font-medium text-charcoal hover:text-ink transition-colors">Profile</a>
          }
          @for (l of links(); track l.to) {
            <a [routerLink]="l.to" class="text-sm font-medium text-charcoal hover:text-ink transition-colors">{{ l.label }}</a>
          }
        </nav>

        <div class="flex items-center gap-3">
          @if (user()) {
            <app-notification-bell></app-notification-bell>
            <span class="hidden sm:block text-sm text-charcoal">{{ user()?.firstName }}</span>
            <app-button variant="ghost" (clicked)="handleLogout()">Log out</app-button>
          } @else {
            <a routerLink="/login" class="text-sm font-medium text-charcoal hover:text-ink">Log in</a>
            <a routerLink="/register">
              <app-button variant="accent">Sign up</app-button>
            </a>
          }
        </div>
      </div>
      @if (user()) {
        <nav class="md:hidden flex items-center gap-4 px-6 pb-3 overflow-x-auto">
          <a routerLink="/profile" class="text-sm font-medium text-charcoal whitespace-nowrap">Profile</a>
          @for (l of links(); track l.to) {
            <a [routerLink]="l.to" class="text-sm font-medium text-charcoal whitespace-nowrap">{{ l.label }}</a>
          }
        </nav>
      }
    </header>
  `,
})
export class NavbarComponent {
  constructor(private auth: AuthService, private router: Router) {}

  user = this.auth.user;

  links() {
    const u = this.user();
    return u ? ROLE_LINKS[u.roleCode] || [] : [];
  }

  handleLogout() {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }
}
