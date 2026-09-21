import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FleetComponent } from './fleet.component';
import { PendingLicencesComponent } from './pending-licences.component';
import { PendingVehiclesComponent } from './pending-vehicles.component';
import { FaultedVehiclesComponent } from './faulted-vehicles.component';
import { DealersComponent } from './dealers.component';
import { UsersComponent } from './users.component';
import { BookingsComponent } from './bookings.component';
import { PayoutsComponent } from './payouts.component';
import { CouponsComponent } from './coupons.component';
import { ReviewsComponent } from './reviews.component';
import { AuditLogComponent } from './audit-log.component';

const TABS = [
  { key: 'fleet', label: 'Fleet & analytics' },
  { key: 'licences', label: 'Licences' },
  { key: 'vehicles', label: 'Vehicles' },
  { key: 'faulted', label: 'Faulted vehicles' },
  { key: 'dealers', label: 'Dealers' },
  { key: 'users', label: 'Users' },
  { key: 'bookings', label: 'Bookings' },
  { key: 'payouts', label: 'Payouts' },
  { key: 'coupons', label: 'Coupons' },
  { key: 'reviews', label: 'Reviews' },
  { key: 'audit', label: 'Audit log' },
];

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FleetComponent,
    PendingLicencesComponent,
    PendingVehiclesComponent,
    FaultedVehiclesComponent,
    DealersComponent,
    UsersComponent,
    BookingsComponent,
    PayoutsComponent,
    CouponsComponent,
    ReviewsComponent,
    AuditLogComponent,
  ],
  template: `
    <div class="max-w-6xl mx-auto px-6 py-12">
      <h1 class="font-display text-2xl font-bold mb-8">Admin</h1>

      <div class="flex gap-1 border-b border-mist mb-8 overflow-x-auto">
        @for (t of tabs; track t.key) {
          <button
            (click)="active = t.key"
            class="px-4 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition-colors"
            [ngClass]="active === t.key ? 'border-marigold text-ink' : 'border-transparent text-charcoal hover:text-ink'"
          >
            {{ t.label }}
          </button>
        }
      </div>

      @switch (active) {
        @case ('fleet') { <app-fleet></app-fleet> }
        @case ('licences') { <app-pending-licences></app-pending-licences> }
        @case ('vehicles') { <app-pending-vehicles></app-pending-vehicles> }
        @case ('faulted') { <app-faulted-vehicles></app-faulted-vehicles> }
        @case ('dealers') { <app-dealers></app-dealers> }
        @case ('users') { <app-users></app-users> }
        @case ('bookings') { <app-bookings></app-bookings> }
        @case ('payouts') { <app-payouts></app-payouts> }
        @case ('coupons') { <app-coupons></app-coupons> }
        @case ('reviews') { <app-reviews></app-reviews> }
        @case ('audit') { <app-audit-log></app-audit-log> }
      }
    </div>
  `,
})
export class AdminDashboardComponent {
  tabs = TABS;
  active = 'fleet';
}
