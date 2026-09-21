import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatUGX, formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { StatusPillComponent } from '../../shared/status-pill.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-my-earnings',
  standalone: true,
  imports: [CommonModule, CardComponent, StatusPillComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    <div class="max-w-3xl mx-auto px-6 py-12">
      <h1 class="font-display text-2xl font-bold mb-2">My earnings</h1>
      <p class="text-charcoal mb-8">Your share of each completed booking, after the platform commission.</p>

      @if (loading) {
        <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
      }

      <app-alert className="mb-4" [message]="error"></app-alert>

      @if (!loading && earnings.length === 0) {
        <app-empty-state title="No earnings yet" description="Earnings appear here once a rental on one of your vehicles is completed."></app-empty-state>
      }

      @if (!loading && earnings.length > 0) {
        <app-card className="p-5 mb-6 flex items-center justify-between">
          <span class="text-charcoal">Pending payout total</span>
          <span class="font-display text-xl font-bold">{{ formatUGX(totalPending()) }}</span>
        </app-card>

        <div class="space-y-3">
          @for (e of earnings; track e.id) {
            <app-card className="p-5 flex items-center justify-between">
              <div>
                <p class="font-medium">Booking #{{ e.bookingId }}</p>
                <p class="text-sm text-charcoal">{{ formatDate(e.createdAt) }} · {{ e.commissionPercentage }}% platform fee</p>
              </div>
              <div class="text-right">
                <app-status-pill [status]="e.payoutStatus"></app-status-pill>
                <p class="font-display font-semibold mt-1">{{ formatUGX(e.ownerAmount) }}</p>
              </div>
            </app-card>
          }
        </div>
      }
    </div>
  `,
})
export class MyEarningsComponent implements OnInit {
  earnings: any[] = [];
  loading = true;
  error = '';
  formatUGX = formatUGX;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  async ngOnInit() {
    try {
      const result = await this.api.call<any[]>('EarningService', 'myEarnings', {}, this.auth.token());
      this.earnings = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  totalPending() {
    return this.earnings.filter((e) => e.payoutStatus === 'PENDING').reduce((sum, e) => sum + Number(e.ownerAmount || 0), 0);
  }
}
