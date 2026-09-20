import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { VehiclePhotoComponent } from '../../shared/vehicle-photo.component';
import { PlateBadgeComponent } from '../../shared/plate-badge.component';
import { CardComponent } from '../../shared/card.component';
import { ButtonComponent } from '../../shared/button.component';
import { InputComponent } from '../../shared/input.component';
import { StatusPillComponent } from '../../shared/status-pill.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-faulted-vehicles',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    VehiclePhotoComponent,
    PlateBadgeComponent,
    CardComponent,
    ButtonComponent,
    InputComponent,
    StatusPillComponent,
    AlertComponent,
    SpinnerComponent,
    EmptyStateComponent,
  ],
  template: `
    <div>
      <app-card className="p-5 mb-6 max-w-md">
        <h3 class="font-display font-semibold text-sm mb-3">Flag a vehicle for maintenance</h3>
        <form (submit)="flagUnderMaintenance($event)" class="flex gap-3">
          <app-input placeholder="Vehicle ID" type="number" name="flagId" [(ngModel)]="flagId"></app-input>
          <app-button variant="outline" type="submit" [disabled]="flagging">
            @if (flagging) { <app-spinner></app-spinner> } @else { Flag }
          </app-button>
        </form>
      </app-card>

      @if (loading) {
        <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
      }

      <app-alert className="mb-4" [message]="error"></app-alert>

      @if (!loading && vehicles.length === 0) {
        <app-empty-state title="No faulted vehicles" description="Rejected inspections or vehicles flagged for maintenance appear here."></app-empty-state>
      }

      <div class="grid sm:grid-cols-2 gap-4">
        @for (v of vehicles; track v.id) {
          <app-card className="p-5">
            <div class="flex items-start justify-between mb-2">
              <app-vehicle-photo [vehicleId]="v.id" [category]="null" className="w-16 h-12"></app-vehicle-photo>
              <app-status-pill [status]="v.mechanicalStatus"></app-status-pill>
            </div>
            <h3 class="font-display font-semibold">{{ v.make }} {{ v.model }}</h3>
            <div class="flex items-center justify-between mt-1 mb-3">
              <app-plate-badge [plate]="v.plateNumber"></app-plate-badge>
              <span class="text-sm text-charcoal">{{ v.location }}</span>
            </div>
            <app-button variant="accent" [disabled]="busyId === v.id" (clicked)="markRepaired(v.id)">
              @if (busyId === v.id) { <app-spinner></app-spinner> } @else { Mark repaired - send for re-inspection }
            </app-button>
          </app-card>
        }
      </div>
    </div>
  `,
})
export class FaultedVehiclesComponent implements OnInit {
  vehicles: any[] = [];
  loading = true;
  error = '';
  busyId: number | null = null;
  flagId = '';
  flagging = false;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const filters = [{ key: 'mechanicalStatus', operator: 'IN', fieldType: 'STRING', values: ['REJECTED', 'UNDER_MAINTENANCE'] }];
      const result = await this.api.call<any>('VehicleService', 'search', { SEARCH: { filters, page: 0, size: 100 } }, this.auth.token());
      this.vehicles = result?.content || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  async markRepaired(id: number) {
    this.busyId = id;
    try {
      await this.api.call('VehicleService', 'markRepaired', { vehicle_id: id }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }

  async flagUnderMaintenance(e: Event) {
    e.preventDefault();
    if (!this.flagId) return;
    this.flagging = true;
    this.error = '';
    try {
      await this.api.call('VehicleService', 'markUnderMaintenance', { vehicle_id: Number(this.flagId) }, this.auth.token());
      this.flagId = '';
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.flagging = false;
    }
  }
}
