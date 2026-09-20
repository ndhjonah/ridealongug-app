import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { formatUGX, formatDate } from '../utils/format';
import { validateLicenceNumber, runValidators, hasErrors } from '../utils/validation';
import { CardComponent } from '../shared/card.component';
import { InputComponent } from '../shared/input.component';
import { SelectComponent } from '../shared/select.component';
import { ButtonComponent } from '../shared/button.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';
import { StatusPillComponent } from '../shared/status-pill.component';
import { EmptyStateComponent } from '../shared/empty-state.component';

const LICENCE_CLASSES = ['A', 'B', 'C', 'D', 'E'];

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardComponent,
    InputComponent,
    SelectComponent,
    ButtonComponent,
    AlertComponent,
    SpinnerComponent,
    StatusPillComponent,
    EmptyStateComponent,
  ],
  template: `
    @if (dashboard === undefined) {
      <div class="flex justify-center py-24">
        <app-spinner className="w-6 h-6 text-charcoal"></app-spinner>
      </div>
    } @else if (dashboard === null) {
      <div class="max-w-lg mx-auto px-6 py-24 text-center">
        <app-alert className="mb-4" [message]="loadError"></app-alert>
        <app-button variant="outline" (clicked)="load()">Try again</app-button>
      </div>
    } @else {
      <div class="max-w-3xl mx-auto px-6 py-12 space-y-8">
        <div>
          <h1 class="font-display text-2xl font-bold mb-2">Driver dashboard</h1>
          <p class="text-charcoal text-sm">Everything about your driving activity on RideAlongUG, in one place.</p>
        </div>

        @if (!dashboard.profile) {
          <app-card className="p-6">
            <h2 class="font-display font-semibold mb-4">Set up your driver profile</h2>
            <form (submit)="submitProfile($event)" class="space-y-4" novalidate>
              <app-input label="Licence number" name="licence_number" [(ngModel)]="profileForm.licence_number"
                [error]="profileFieldErrors.licence_number || ''" [required]="true"></app-input>
              <app-select label="Licence class" name="licence_class" [(ngModel)]="profileForm.licence_class">
                @for (c of licenceClasses; track c) { <option [value]="c">{{ c }}</option> }
              </app-select>
              <app-input label="Years of experience" type="number" min="0" name="years" [(ngModel)]="profileForm.years_of_experience"></app-input>
              <app-alert [message]="profileError"></app-alert>
              <app-alert type="success" [message]="profileMessage"></app-alert>
              <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="profileLoading">
                @if (profileLoading) { <app-spinner></app-spinner> } @else { Save profile }
              </app-button>
            </form>
          </app-card>
        } @else {
          <app-card className="p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-display font-semibold">Availability</h2>
              <app-status-pill [status]="dashboard.isAvailable ? 'APPROVED' : 'UNDER_MAINTENANCE'"></app-status-pill>
            </div>
            <p class="text-sm text-charcoal mb-4">
              @if (dashboard.currentJourney) {
                You're on an active journey, so you're automatically marked unavailable until it's completed.
              } @else {
                Toggle whether you can be assigned new journeys right now.
              }
            </p>
            <app-alert className="mb-3" [message]="availError"></app-alert>
            <app-alert type="success" className="mb-3" [message]="availMessage"></app-alert>
            <div class="flex gap-3">
              <app-button [variant]="dashboard.isAvailable ? 'accent' : 'outline'" [disabled]="availLoading || !!dashboard.currentJourney" (clicked)="toggleAvailability(true)">
                @if (availLoading) { <app-spinner></app-spinner> } @else { Available }
              </app-button>
              <app-button [variant]="!dashboard.isAvailable ? 'accent' : 'outline'" [disabled]="availLoading" (clicked)="toggleAvailability(false)">
                @if (availLoading) { <app-spinner></app-spinner> } @else { Unavailable }
              </app-button>
            </div>
          </app-card>

          <app-card className="p-6">
            <h2 class="font-display font-semibold mb-4">Current journey</h2>
            @if (dashboard.currentJourney; as j) {
              <div class="space-y-2 text-sm">
                <p class="font-medium">Booking #{{ j.id }} · Vehicle #{{ j.vehicleId }}</p>
                <p class="text-charcoal">{{ j.pickupLocation }} → {{ j.dropoffLocation }}</p>
                <p class="text-charcoal">{{ formatDate(j.startDate) }} – {{ formatDate(j.endDate) }}</p>
                <app-status-pill [status]="j.status"></app-status-pill>
              </div>
            } @else {
              <app-empty-state title="No active journey" description="You'll see it here as soon as you're assigned one."></app-empty-state>
            }
          </app-card>

          <div class="grid sm:grid-cols-2 gap-4">
            <app-card className="p-6">
              <p class="text-sm text-charcoal mb-1">Total earnings</p>
              <p class="font-display text-2xl font-semibold">{{ formatUGX(dashboard.totalEarnings) }}</p>
              <p class="text-xs text-charcoal mt-1">From {{ completedTripsCount() }} completed trip(s)</p>
            </app-card>
            <app-card className="p-6">
              <p class="text-sm text-charcoal mb-1">Trips made</p>
              <p class="font-display text-2xl font-semibold">{{ (dashboard.trips || []).length }}</p>
              <p class="text-xs text-charcoal mt-1">{{ (dashboard.carsUsed || []).length }} distinct car(s) driven</p>
            </app-card>
          </div>

          <app-card className="p-6">
            <h2 class="font-display font-semibold mb-4">Cars assigned, by class</h2>
            @if (classEntries().length === 0) {
              <app-empty-state title="No cars assigned yet"></app-empty-state>
            } @else {
              <ul class="space-y-2 text-sm">
                @for (entry of classEntries(); track entry[0]) {
                  <li class="flex justify-between border-b border-mist last:border-0 pb-2 last:pb-0">
                    <span>{{ entry[0] }}</span>
                    <span class="font-medium">{{ entry[1] }}</span>
                  </li>
                }
              </ul>
            }
          </app-card>

          <app-card className="p-6">
            <h2 class="font-display font-semibold mb-4">Cars you've driven</h2>
            @if ((dashboard.carsUsed || []).length === 0) {
              <app-empty-state title="No cars yet"></app-empty-state>
            } @else {
              <ul class="space-y-2 text-sm">
                @for (v of dashboard.carsUsed; track v.id) {
                  <li class="flex justify-between border-b border-mist last:border-0 pb-2 last:pb-0">
                    <span>{{ v.make }} {{ v.model }} ({{ v.year }})</span>
                    <span class="text-charcoal">{{ v.plateNumber }}</span>
                  </li>
                }
              </ul>
            }
          </app-card>

          <app-card className="p-6">
            <h2 class="font-display font-semibold mb-4">Trip history</h2>
            @if ((dashboard.trips || []).length === 0) {
              <app-empty-state title="No trips yet"></app-empty-state>
            } @else {
              <ul class="space-y-3 text-sm">
                @for (t of dashboard.trips; track t.id) {
                  <li class="flex items-center justify-between border-b border-mist last:border-0 pb-3 last:pb-0">
                    <div>
                      <p class="font-medium">Booking #{{ t.id }} · Vehicle #{{ t.vehicleId }}</p>
                      <p class="text-charcoal text-xs">{{ formatDate(t.startDate) }} – {{ formatDate(t.endDate) }}</p>
                    </div>
                    <app-status-pill [status]="t.status"></app-status-pill>
                  </li>
                }
              </ul>
            }
          </app-card>
        }
      </div>
    }
  `,
})
export class DriverDashboardComponent implements OnInit {
  dashboard: any = undefined;
  loadError = '';

  profileForm = { licence_number: '', licence_class: 'B', years_of_experience: '' };
  profileFieldErrors: Record<string, string> = {};
  profileMessage = '';
  profileError = '';
  profileLoading = false;

  availMessage = '';
  availError = '';
  availLoading = false;

  licenceClasses = LICENCE_CLASSES;
  formatUGX = formatUGX;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loadError = '';
    try {
      const result = await this.api.call<any>('DriverProfileService', 'myDashboard', {}, this.auth.token());
      this.dashboard = result;
    } catch (err: any) {
      this.loadError = err.message || 'Could not reach the server. Please check your connection and try again.';
      this.dashboard = null;
    }
  }

  completedTripsCount() {
    return (this.dashboard?.trips || []).filter((t: any) => t.status === 'COMPLETED').length;
  }

  classEntries() {
    return Object.entries(this.dashboard?.carsAssignedByClass || {});
  }

  async submitProfile(e: Event) {
    e.preventDefault();
    this.profileError = '';
    this.profileMessage = '';

    const errors = runValidators(this.profileForm, { licence_number: validateLicenceNumber });
    this.profileFieldErrors = errors as Record<string, string>;
    if (hasErrors(errors)) return;

    this.profileLoading = true;
    try {
      await this.api.call(
        'DriverProfileService',
        'createProfile',
        {
          licence_number: this.profileForm.licence_number,
          licence_class: this.profileForm.licence_class,
          years_of_experience: Number(this.profileForm.years_of_experience) || 0,
        },
        this.auth.token()
      );
      this.profileMessage = 'Driver profile created.';
      await this.load();
    } catch (err: any) {
      this.profileError = err.message;
    } finally {
      this.profileLoading = false;
    }
  }

  async toggleAvailability(nextAvailable: boolean) {
    this.availError = '';
    this.availMessage = '';
    this.availLoading = true;
    try {
      await this.api.call('DriverProfileService', 'updateAvailability', { is_available: nextAvailable }, this.auth.token());
      this.availMessage = `Marked as ${nextAvailable ? 'available' : 'unavailable'}.`;
      await this.load();
    } catch (err: any) {
      this.availError = err.message;
    } finally {
      this.availLoading = false;
    }
  }
}
