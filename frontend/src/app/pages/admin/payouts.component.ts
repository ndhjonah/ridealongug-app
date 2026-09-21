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
  selector: 'app-payouts',
  standalone: true,
  imports: [CommonModule, CardComponent, ButtonComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>
        @if (payouts.length === 0) {
          <app-empty-state title="No pending payouts" description="Owner and dealer payouts appear here once a booking completes."></app-empty-state>
        } @else {
          <div class="space-y-3">
            @for (p of payouts; track p.id) {
              <app-card className="p-5 flex items-center justify-between">
                <div>
                  <p class="font-medium">Owner #{{ p.ownerId }} · Booking #{{ p.bookingId }}</p>
                  <p class="text-sm text-charcoal">{{ formatDate(p.createdAt) }} · {{ p.commissionPercentage }}% platform fee</p>
                </div>
                <div class="flex items-center gap-3">
                  <span class="font-display font-semibold">{{ formatUGX(p.ownerAmount) }}</span>
                  <app-button variant="accent" [disabled]="busyId === p.id" (clicked)="markPaid(p.id)">
                    @if (busyId === p.id) { <app-spinner></app-spinner> } @else { Mark as paid }
                  </app-button>
                </div>
              </app-card>
            }
          </div>
        }
      </div>
    }
  `,
})
export class PayoutsComponent implements OnInit {
  payouts: any[] = [];
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
    try {
      const result = await this.api.call<any[]>('EarningService', 'pendingPayouts', {}, this.auth.token());
      this.payouts = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  async markPaid(id: number) {
    this.busyId = id;
    try {
      await this.api.call('EarningService', 'markAsPaid', { id }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }
}
