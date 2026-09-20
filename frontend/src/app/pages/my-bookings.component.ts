import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { formatUGX, formatDate } from '../utils/format';
import { CardComponent } from '../shared/card.component';
import { StatusPillComponent } from '../shared/status-pill.component';
import { ButtonComponent } from '../shared/button.component';
import { SelectComponent } from '../shared/select.component';
import { EmptyStateComponent } from '../shared/empty-state.component';
import { ModalComponent } from '../shared/modal.component';
import { InputComponent } from '../shared/input.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';

const PAYMENT_METHODS = [
  { value: 'MOBILE_MONEY', label: 'Mobile Money' },
  { value: 'CARD', label: 'Card' },
  { value: 'CASH', label: 'Cash' },
];

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    CardComponent,
    StatusPillComponent,
    ButtonComponent,
    SelectComponent,
    EmptyStateComponent,
    ModalComponent,
    InputComponent,
    AlertComponent,
    SpinnerComponent,
  ],
  template: `
    <div class="max-w-4xl mx-auto px-6 py-12">
      <h1 class="font-display text-2xl font-bold mb-8">My bookings</h1>

      @if (loading) {
        <div class="flex justify-center py-16">
          <app-spinner className="w-6 h-6 text-charcoal"></app-spinner>
        </div>
      }

      <app-alert className="mb-4" [message]="error"></app-alert>

      @if (!loading && bookings.length === 0) {
        <app-empty-state title="No bookings yet" description="Browse the fleet and book your first ride.">
          <a routerLink="/"><app-button variant="accent">Browse vehicles</app-button></a>
        </app-empty-state>
      }

      <div class="space-y-4">
        @for (b of bookings; track b.id) {
          <app-card className="p-5">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="font-display font-semibold text-ink">Booking #{{ b.id }}</p>
                <p class="text-sm text-charcoal">{{ b.pickupLocation }} → {{ b.dropoffLocation }}</p>
                <p class="text-sm text-charcoal">{{ formatDate(b.startDate) }} – {{ formatDate(b.endDate) }}</p>
                @if (b.status === 'PENDING') {
                  <p class="text-xs text-brick mt-1">Pick up the car and pay to move this booking forward</p>
                }
                @if (b.status === 'PICKUP_PENDING') {
                  <p class="text-xs text-marigold mt-1">Payment received - waiting for the vehicle owner to confirm handover</p>
                }
                @if (b.status === 'CONFIRMED') {
                  <p class="text-xs text-savanna mt-1">Pickup confirmed - start your trip before {{ formatDate(b.startDate) }}</p>
                }
                @if (b.status === 'ONGOING') {
                  <p class="text-xs text-brick mt-1">Return the vehicle before {{ formatDate(b.endDate) }} to avoid a late return</p>
                }
              </div>
              <div class="text-right">
                <app-status-pill [status]="b.status"></app-status-pill>
                <p class="font-display font-semibold mt-2">{{ formatUGX(b.totalCost) }}</p>
              </div>
            </div>

            <div class="flex gap-2 mt-4">
              @if (b.status === 'PENDING') {
                <app-button variant="accent" (clicked)="openPay(b)">Pick up car (pay now)</app-button>
              }
              @if (b.status === 'CONFIRMED') {
                <app-button variant="accent" [disabled]="busyId === b.id" (clicked)="runAction('startTrip', b.id)">
                  @if (busyId === b.id) { <app-spinner></app-spinner> } @else { Start trip }
                </app-button>
              }
              @if (b.status === 'ONGOING') {
                <app-button variant="accent" [disabled]="busyId === b.id" (clicked)="runAction('completeBooking', b.id)">
                  @if (busyId === b.id) { <app-spinner></app-spinner> } @else { Complete trip }
                </app-button>
              }
              @if (b.status === 'PENDING' || b.status === 'PICKUP_PENDING' || b.status === 'CONFIRMED') {
                <app-button variant="outline" [disabled]="busyId === b.id" (clicked)="runAction('cancelBooking', b.id)">
                  @if (busyId === b.id) { <app-spinner></app-spinner> } @else { Cancel }
                </app-button>
              }
              @if (b.status === 'COMPLETED') {
                <app-button variant="ghost" (clicked)="openReview(b)">Leave a review</app-button>
              }
            </div>
          </app-card>
        }
      </div>

      <app-modal [open]="!!payBooking" [title]="'Pay for booking #' + payBooking?.id" (close)="payBooking = null">
        <div class="space-y-4">
          <p class="text-sm text-charcoal">
            Amount due: <span class="font-semibold text-ink">{{ formatUGX(payBooking?.totalCost) }}</span>
          </p>
          <app-select label="Payment method" name="paymentMethod" [(ngModel)]="paymentMethod">
            @for (m of paymentMethods; track m.value) {
              <option [value]="m.value">{{ m.label }}</option>
            }
          </app-select>
          <app-alert [message]="payError"></app-alert>
          <app-button variant="accent" [fullWidth]="true" [disabled]="paying" (clicked)="submitPayment()">
            @if (paying) { <app-spinner></app-spinner> } @else { Confirm payment }
          </app-button>
          <p class="text-xs text-charcoal text-center">
            This is a simulated payment for demo purposes - your booking will be confirmed immediately.
          </p>
        </div>
      </app-modal>

      <app-modal [open]="!!reviewBooking" [title]="'Review booking #' + reviewBooking?.id" (close)="reviewBooking = null">
        <div class="space-y-4">
          <app-input label="Rating (1-5)" type="number" min="1" max="5" name="rating" [(ngModel)]="reviewForm.vehicle_rating"></app-input>
          <app-input label="Comment" name="comment" [(ngModel)]="reviewForm.comment"></app-input>
          <app-alert [message]="reviewError"></app-alert>
          <app-button variant="accent" [fullWidth]="true" (clicked)="submitReview()">Submit review</app-button>
        </div>
      </app-modal>
    </div>
  `,
})
export class MyBookingsComponent implements OnInit {
  bookings: any[] = [];
  loading = true;
  error = '';
  busyId: number | null = null;

  reviewBooking: any = null;
  reviewForm = { vehicle_rating: '5', comment: '' };
  reviewError = '';

  payBooking: any = null;
  paymentMethod = 'MOBILE_MONEY';
  payError = '';
  paying = false;

  paymentMethods = PAYMENT_METHODS;
  formatUGX = formatUGX;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const result = await this.api.call<any[]>('BookingService', 'myBookings', {}, this.auth.token());
      this.bookings = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  async runAction(action: string, id: number) {
    this.busyId = id;
    this.error = '';
    try {
      await this.api.call('BookingService', action, { id }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }

  openPay(b: any) {
    this.payBooking = b;
    this.payError = '';
  }

  openReview(b: any) {
    this.reviewBooking = b;
  }

  async submitPayment() {
    this.payError = '';
    this.paying = true;
    try {
      await this.api.call(
        'PaymentService',
        'makePayment',
        { booking_id: this.payBooking.id, amount: this.payBooking.totalCost, payment_method: this.paymentMethod },
        this.auth.token()
      );
      this.payBooking = null;
      await this.load();
    } catch (err: any) {
      this.payError = err.message;
    } finally {
      this.paying = false;
    }
  }

  async submitReview() {
    this.reviewError = '';
    try {
      await this.api.call(
        'ReviewService',
        'leaveReview',
        {
          booking_id: this.reviewBooking.id,
          vehicle_rating: Number(this.reviewForm.vehicle_rating),
          comment: this.reviewForm.comment,
        },
        this.auth.token()
      );
      this.reviewBooking = null;
      this.reviewForm = { vehicle_rating: '5', comment: '' };
    } catch (err: any) {
      this.reviewError = err.message;
    }
  }
}
