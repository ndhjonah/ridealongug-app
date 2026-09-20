import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatUGX, formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-pending-pickups',
  standalone: true,
  imports: [CommonModule, CardComponent, ButtonComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    <div class="max-w-3xl mx-auto px-6 py-12">
      <h1 class="font-display text-2xl font-bold mb-2">Pending pickups</h1>
      <p class="text-charcoal text-sm mb-8">
        Customers who've paid and are on their way to collect one of your vehicles. Confirm once
        you've physically handed over the car and verified payment.
      </p>

      @if (loading) {
        <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
      }

      <app-alert className="mb-4" [message]="error"></app-alert>

      @if (!loading && bookings.length === 0) {
        <app-empty-state title="Nothing to confirm right now" description="Paid bookings awaiting pickup will show up here."></app-empty-state>
      }

      <div class="space-y-4">
        @for (b of bookings; track b.id) {
          <app-card className="p-5 flex items-center justify-between gap-4">
            <div>
              <p class="font-display font-semibold">Booking #{{ b.id }} · Vehicle #{{ b.vehicleId }}</p>
              <p class="text-sm text-charcoal">{{ formatDate(b.startDate) }} – {{ formatDate(b.endDate) }}</p>
              <p class="text-sm text-charcoal">{{ b.pickupLocation }} → {{ b.dropoffLocation }}</p>
            </div>
            <div class="text-right">
              <p class="font-display font-semibold mb-2">{{ formatUGX(b.totalCost) }}</p>
              <app-button variant="accent" [disabled]="busyId === b.id" (clicked)="confirmPickup(b.id)">
                @if (busyId === b.id) { <app-spinner></app-spinner> } @else { Confirm pickup }
              </app-button>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
})
export class PendingPickupsComponent implements OnInit {
  bookings: any[] = [];
  loading = true;
  error = '';
  busyId: number | null = null;
  formatUGX = formatUGX;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    this.error = '';
    try {
      const result = await this.api.call<any[]>('BookingService', 'pendingPickupsForOwner', {}, this.auth.token());
      this.bookings = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  async confirmPickup(id: number) {
    this.busyId = id;
    this.error = '';
    try {
      await this.api.call('BookingService', 'confirmPickup', { id }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }
}
