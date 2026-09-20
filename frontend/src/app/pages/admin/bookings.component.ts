import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, searchPayload } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatUGX, formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { ButtonComponent } from '../../shared/button.component';
import { SelectComponent } from '../../shared/select.component';
import { StatusPillComponent } from '../../shared/status-pill.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { RouteMapModalComponent } from '../../shared/route-map-modal.component';

const NEXT_ACTION: Record<string, { action: string; label: string }> = {
  ONGOING: { action: 'completeBooking', label: 'Mark complete' },
};

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardComponent,
    ButtonComponent,
    SelectComponent,
    StatusPillComponent,
    AlertComponent,
    SpinnerComponent,
    EmptyStateComponent,
    RouteMapModalComponent,
  ],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>
        @if (bookings.length === 0) {
          <app-empty-state title="No bookings yet" description="Bookings will appear here as customers make them."></app-empty-state>
        } @else {
          <div class="space-y-3">
            @for (b of bookings; track b.id) {
              <app-card className="p-5 flex flex-col gap-3">
                <div class="flex items-center justify-between gap-4">
                  <button
                    class="text-left disabled:cursor-default"
                    [disabled]="!isRented(b)"
                    (click)="isRented(b) && (mapBooking = b)"
                  >
                    <p class="font-medium" [ngClass]="isRented(b) ? 'underline decoration-dotted' : ''">
                      Booking #{{ b.id }} · Vehicle #{{ b.vehicleId }} · Customer #{{ b.customerId }}
                    </p>
                    <p class="text-sm text-charcoal">{{ formatDate(b.startDate) }} – {{ formatDate(b.endDate) }}</p>
                    @if (b.withDriver) {
                      <p class="text-xs text-charcoal mt-0.5">
                        {{ b.driverId ? 'Driver #' + b.driverId + ' assigned by customer' : 'Requested a driver' }}
                      </p>
                    }
                    @if (b.status === 'PENDING') {
                      <p class="text-xs mt-0.5" [ngClass]="paidStatusByBooking[b.id] ? 'text-savanna' : 'text-brick'">
                        {{ paidStatusByBooking[b.id] ? 'Payment received' : 'Payment not yet received' }}
                      </p>
                    }
                    @if (b.status === 'PICKUP_PENDING') {
                      <p class="text-xs text-marigold mt-0.5">Awaiting owner's pickup confirmation</p>
                    }
                    @if (isRented(b)) {
                      <p class="text-xs text-marigold mt-0.5">Click to view route on map</p>
                    }
                  </button>
                  <div class="flex items-center gap-3">
                    <app-status-pill [status]="b.status"></app-status-pill>
                    <span class="font-display font-semibold">{{ formatUGX(b.totalCost) }}</span>
                    @if (nextAction(b); as next) {
                      <app-button variant="accent" [disabled]="busyId === b.id" (clicked)="runAction(next.action, b.id)">
                        @if (busyId === b.id) { <app-spinner></app-spinner> } @else { {{ next.label }} }
                      </app-button>
                    }
                    @if (b.status === 'PENDING' || b.status === 'PICKUP_PENDING' || b.status === 'CONFIRMED') {
                      <app-button variant="outline" [disabled]="busyId === b.id" (clicked)="runAction('cancelBooking', b.id)">Cancel</app-button>
                    }
                  </div>
                </div>

                @if (canReassignDriver(b)) {
                  <div class="flex items-center gap-3 bg-mist/40 rounded-sign p-3">
                    <app-select className="w-auto flex-1" name="reassign-{{ b.id }}" [ngModel]="selectedDriverByBooking[b.id] || ''"
                      (valueChange)="selectedDriverByBooking[b.id] = $event">
                      <option value="">Select a driver to reassign…</option>
                      @for (d of availableDrivers; track d.id) {
                        <option [value]="d.id">
                          Driver #{{ d.id }} · Licence {{ d.licenceNumber }} ({{ d.licenceClass }}) · {{ d.yearsOfExperience ?? 0 }} yrs
                        </option>
                      }
                    </app-select>
                    <app-button variant="outline" [disabled]="busyId === b.id" (clicked)="assignDriver(b.id)">
                      @if (busyId === b.id) { <app-spinner></app-spinner> } @else { Reassign driver }
                    </app-button>
                  </div>
                }
                @if (canReassignDriver(b) && availableDrivers.length === 0) {
                  <p class="text-xs text-brick">No other drivers are currently available on the platform.</p>
                }
              </app-card>
            }
          </div>
        }

        <app-route-map-modal [booking]="mapBooking" (close)="mapBooking = null"></app-route-map-modal>
      </div>
    }
  `,
})
export class BookingsComponent implements OnInit {
  bookings: any[] = [];
  availableDrivers: any[] = [];
  selectedDriverByBooking: Record<number, string> = {};
  paidStatusByBooking: Record<number, boolean> = {};
  loading = true;
  error = '';
  busyId: number | null = null;
  mapBooking: any = null;

  formatUGX = formatUGX;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  isRented(b: any) {
    return b.status === 'ONGOING' || b.status === 'COMPLETED';
  }

  canReassignDriver(b: any) {
    return b.withDriver && b.status !== 'COMPLETED' && b.status !== 'CANCELLED';
  }

  nextAction(b: any) {
    return NEXT_ACTION[b.status];
  }

  async load() {
    this.loading = true;
    this.error = '';
    try {
      const [bookingsResult, driversResult] = await Promise.all([
        this.api.call<any>('BookingService', 'search', searchPayload(), this.auth.token()),
        this.api.call<any[]>('DriverProfileService', 'listAvailable', {}, this.auth.token()).catch(() => []),
      ]);
      const content = bookingsResult?.content || [];
      this.bookings = content;
      this.availableDrivers = driversResult || [];

      const pendingIds = content.filter((b: any) => b.status === 'PENDING').map((b: any) => b.id);
      const paidEntries = await Promise.all(
        pendingIds.map(async (id: number) => {
          try {
            const payments = await this.api.call<any[]>('PaymentService', 'paymentsForBooking', { booking_id: id }, this.auth.token());
            const isPaid = (payments || []).some((p) => p.paymentStatus === 'COMPLETED');
            return [id, isPaid] as const;
          } catch {
            return [id, false] as const;
          }
        })
      );
      this.paidStatusByBooking = Object.fromEntries(paidEntries);
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

  async assignDriver(bookingId: number) {
    const driverId = this.selectedDriverByBooking[bookingId];
    if (!driverId) {
      this.error = 'Pick a driver from the dropdown first.';
      return;
    }
    this.busyId = bookingId;
    this.error = '';
    try {
      await this.api.call('BookingService', 'reassignDriver', { booking_id: bookingId, driver_id: Number(driverId) }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }
}
