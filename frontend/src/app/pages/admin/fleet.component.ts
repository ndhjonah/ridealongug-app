import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatUGX, formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { ButtonComponent } from '../../shared/button.component';
import { SelectComponent } from '../../shared/select.component';
import { InputComponent } from '../../shared/input.component';
import { StatusPillComponent } from '../../shared/status-pill.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { PlateBadgeComponent } from '../../shared/plate-badge.component';
import { VehiclePhotoComponent } from '../../shared/vehicle-photo.component';

const STATUS_FILTERS = [
  { value: '', label: 'All statuses' },
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'ON_TRIP', label: 'On a trip' },
  { value: 'UNDER_MAINTENANCE', label: 'Under maintenance' },
  { value: 'HIDDEN', label: 'Hidden from listings' },
];

@Component({
  selector: 'app-fleet',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardComponent,
    ButtonComponent,
    SelectComponent,
    InputComponent,
    StatusPillComponent,
    AlertComponent,
    SpinnerComponent,
    EmptyStateComponent,
    PlateBadgeComponent,
    VehiclePhotoComponent,
  ],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>

        <div class="grid grid-cols-2 sm:grid-cols-5 gap-3 mb-6">
          <app-card className="p-4">
            <p class="text-xs text-charcoal">Total fleet</p>
            <p class="font-display text-xl font-semibold">{{ summary().total }}</p>
          </app-card>
          <app-card className="p-4">
            <p class="text-xs text-charcoal">Available</p>
            <p class="font-display text-xl font-semibold text-savanna">{{ summary().available }}</p>
          </app-card>
          <app-card className="p-4">
            <p class="text-xs text-charcoal">On a trip</p>
            <p class="font-display text-xl font-semibold text-ink">{{ summary().onTrip }}</p>
          </app-card>
          <app-card className="p-4">
            <p class="text-xs text-charcoal">Maintenance</p>
            <p class="font-display text-xl font-semibold text-brick">{{ summary().maintenance }}</p>
          </app-card>
          <app-card className="p-4">
            <p class="text-xs text-charcoal">Total revenue</p>
            <p class="font-display text-xl font-semibold">{{ formatUGX(summary().totalRevenue) }}</p>
          </app-card>
        </div>

        <div class="flex flex-col sm:flex-row gap-3 mb-6">
          <app-input placeholder="Search make, model, plate, location…" name="search" [(ngModel)]="search" className="flex-1"></app-input>
          <app-select name="statusFilter" [(ngModel)]="statusFilter" className="sm:w-56">
            @for (f of statusFilters; track f.value) { <option [value]="f.value">{{ f.label }}</option> }
          </app-select>
        </div>

        @if (filtered().length === 0) {
          <app-empty-state title="No vehicles match" description="Try a different search or status filter."></app-empty-state>
        } @else {
          <div class="space-y-3">
            @for (r of filtered(); track r.vehicle.id) {
              <app-card className="p-5">
                <div class="flex items-start justify-between gap-4 flex-wrap">
                  <div class="flex gap-4">
                    <app-vehicle-photo [vehicleId]="r.vehicle.id" [category]="null" className="w-20 h-14 shrink-0"></app-vehicle-photo>
                    <div>
                      <div class="flex items-center gap-2 mb-1">
                        <h3 class="font-display font-semibold">{{ r.vehicle.make }} {{ r.vehicle.model }} ({{ r.vehicle.year }})</h3>
                        <app-plate-badge [plate]="r.vehicle.plateNumber"></app-plate-badge>
                      </div>
                      <p class="text-sm text-charcoal">{{ r.vehicle.location }} · {{ formatUGX(r.vehicle.dailyRate) }}/day</p>
                      <div class="flex items-center gap-2 mt-2">
                        <app-status-pill [status]="r.liveStatus"></app-status-pill>
                        <app-status-pill [status]="r.vehicle.mechanicalStatus"></app-status-pill>
                      </div>
                    </div>
                  </div>

                  <div class="text-sm text-right">
                    <p><span class="text-charcoal">Trips:</span> <span class="font-medium">{{ r.totalTrips }}</span></p>
                    <p><span class="text-charcoal">Distance:</span> <span class="font-medium">{{ Number(r.totalDistanceKm || 0).toFixed(1) }} km</span></p>
                    <p><span class="text-charcoal">Revenue:</span> <span class="font-medium">{{ formatUGX(r.totalRevenue) }}</span></p>
                    @if (r.lastDestination) {
                      <p class="text-charcoal text-xs mt-1">Last seen at {{ r.lastDestination }} ({{ formatDate(r.lastTripEndedAt) }})</p>
                    }
                  </div>
                </div>

                <div class="flex gap-2 mt-4 pt-4 border-t border-mist">
                  <app-button variant="outline" [disabled]="busyId === r.vehicle.id" (clicked)="toggleVisibility(r.vehicle.id, !r.vehicle.isVisible)">
                    @if (busyId === r.vehicle.id) { <app-spinner></app-spinner> } @else { {{ r.vehicle.isVisible ? 'Hide from listings' : 'Show in listings' }} }
                  </app-button>
                  @if (confirmDelete === r.vehicle.id) {
                    <span class="text-xs text-brick self-center">Delete permanently?</span>
                    <app-button variant="danger" [disabled]="busyId === r.vehicle.id" (clicked)="handleDelete(r.vehicle.id)">
                      @if (busyId === r.vehicle.id) { <app-spinner></app-spinner> } @else { Yes, delete }
                    </app-button>
                    <app-button variant="ghost" (clicked)="confirmDelete = null">Cancel</app-button>
                  } @else {
                    <app-button variant="ghost" (clicked)="confirmDelete = r.vehicle.id">Delete</app-button>
                  }
                </div>
                @if (r.hasBookingHistory) {
                  <p class="text-xs text-charcoal mt-2">This vehicle has booking history, so it can only be hidden, not deleted.</p>
                }
              </app-card>
            }
          </div>
        }
      </div>
    }
  `,
})
export class FleetComponent implements OnInit {
  rows: any[] = [];
  loading = true;
  error = '';
  busyId: number | null = null;
  statusFilter = '';
  search = '';
  confirmDelete: number | null = null;

  statusFilters = STATUS_FILTERS;
  formatUGX = formatUGX;
  formatDate = formatDate;
  Number = Number;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    this.error = '';
    try {
      const result = await this.api.call<any[]>('VehicleService', 'fleetOverview', {}, this.auth.token());
      this.rows = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  filtered() {
    return this.rows.filter((r) => {
      if (this.statusFilter && r.liveStatus !== this.statusFilter) return false;
      if (this.search) {
        const q = this.search.toLowerCase();
        const haystack = `${r.vehicle.make} ${r.vehicle.model} ${r.vehicle.plateNumber} ${r.vehicle.location}`.toLowerCase();
        if (!haystack.includes(q)) return false;
      }
      return true;
    });
  }

  summary() {
    return {
      total: this.rows.length,
      available: this.rows.filter((r) => r.liveStatus === 'AVAILABLE').length,
      onTrip: this.rows.filter((r) => r.liveStatus === 'ON_TRIP').length,
      maintenance: this.rows.filter((r) => r.liveStatus === 'UNDER_MAINTENANCE').length,
      totalRevenue: this.rows.reduce((sum, r) => sum + Number(r.totalRevenue || 0), 0),
      totalDistanceKm: this.rows.reduce((sum, r) => sum + Number(r.totalDistanceKm || 0), 0),
    };
  }

  async toggleVisibility(vehicleId: number, nextVisible: boolean) {
    this.busyId = vehicleId;
    this.error = '';
    try {
      await this.api.call('VehicleService', 'setVisibility', { vehicle_id: vehicleId, is_visible: nextVisible }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }

  async handleDelete(vehicleId: number) {
    this.busyId = vehicleId;
    this.error = '';
    try {
      await this.api.call('VehicleService', 'deleteVehicle', { vehicle_id: vehicleId }, this.auth.token());
      this.confirmDelete = null;
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }
}
