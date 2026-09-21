import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, equalsFilter, searchPayload } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { VehiclePhotoComponent } from '../../shared/vehicle-photo.component';
import { CardComponent } from '../../shared/card.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { PlateBadgeComponent } from '../../shared/plate-badge.component';
import { InspectModalComponent } from './inspect-modal.component';

@Component({
  selector: 'app-pending-vehicles',
  standalone: true,
  imports: [CommonModule, VehiclePhotoComponent, CardComponent, ButtonComponent, AlertComponent, SpinnerComponent, EmptyStateComponent, PlateBadgeComponent, InspectModalComponent],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>
        @if (vehicles.length === 0) {
          <app-empty-state title="No vehicles pending inspection" description="Owner and dealer submissions will appear here."></app-empty-state>
        } @else {
          <div class="grid sm:grid-cols-2 gap-4">
            @for (v of vehicles; track v.id) {
              <app-card className="p-5">
                <div class="flex items-start justify-between mb-2">
                  <app-vehicle-photo [vehicleId]="v.id" [category]="null" className="w-16 h-12"></app-vehicle-photo>
                  <app-plate-badge [plate]="v.plateNumber"></app-plate-badge>
                </div>
                <h3 class="font-display font-semibold">{{ v.make }} {{ v.model }}</h3>
                <p class="text-sm text-charcoal mb-3">{{ v.year }} · {{ v.location }}</p>
                <app-button variant="accent" (clicked)="active = v">Inspect &amp; verify</app-button>
              </app-card>
            }
          </div>
        }

        @if (active) {
          <app-inspect-modal [vehicle]="active" (closed)="active = null" (done)="active = null; load()"></app-inspect-modal>
        }
      </div>
    }
  `,
})
export class PendingVehiclesComponent implements OnInit {
  vehicles: any[] = [];
  loading = true;
  error = '';
  active: any = null;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const filters = [equalsFilter('mechanicalStatus', 'STRING', 'PENDING_VERIFICATION')];
      const result = await this.api.call<any>('VehicleService', 'search', searchPayload(filters), this.auth.token());
      this.vehicles = result?.content || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
