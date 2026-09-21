import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-reviews',
  standalone: true,
  imports: [CommonModule, CardComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>
        @if (reviews.length === 0) {
          <app-empty-state title="No reviews yet" description="Customer reviews on completed bookings will appear here."></app-empty-state>
        } @else {
          <div class="space-y-3">
            @for (r of reviews; track r.id) {
              <app-card className="p-5">
                <div class="flex items-center justify-between mb-1">
                  <p class="font-medium">Booking #{{ r.bookingId }} · Customer #{{ r.customerId }}</p>
                  <span class="text-xs text-charcoal">{{ formatDate(r.createdAt) }}</span>
                </div>
                <p class="text-sm text-charcoal mb-2">
                  Vehicle rating: {{ r.vehicleRating }}/5{{ r.driverRating != null ? ' · Driver rating: ' + r.driverRating + '/5' : '' }}
                </p>
                @if (r.comment) {
                  <p class="text-sm italic text-ink">&ldquo;{{ r.comment }}&rdquo;</p>
                }
              </app-card>
            }
          </div>
        }
      </div>
    }
  `,
})
export class ReviewsComponent implements OnInit {
  reviews: any[] = [];
  loading = true;
  error = '';
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  async ngOnInit() {
    try {
      const result = await this.api.call<any[]>('ReviewService', 'allReviews', {}, this.auth.token());
      this.reviews = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
