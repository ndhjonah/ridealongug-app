import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { VehicleGlyphComponent } from '../shared/vehicle-glyph.component';
import { LocationPickerComponent, LatLngPoint } from '../shared/location-picker.component';
import { formatUGX, toEpochMillis, formatDateTimeLocalInputValue } from '../utils/format';
import { haversineKm } from '../utils/geo';
import { PlateBadgeComponent } from '../shared/plate-badge.component';
import { CardComponent } from '../shared/card.component';
import { ButtonComponent } from '../shared/button.component';
import { InputComponent } from '../shared/input.component';
import { SelectComponent } from '../shared/select.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';

const STEPS = ['Trip details', 'Review cost', 'Confirmed'];

@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    VehicleGlyphComponent,
    LocationPickerComponent,
    PlateBadgeComponent,
    CardComponent,
    ButtonComponent,
    InputComponent,
    SelectComponent,
    AlertComponent,
    SpinnerComponent,
  ],
  template: `
    @if (loading) {
      <div class="flex justify-center py-24">
        <app-spinner className="w-6 h-6 text-charcoal"></app-spinner>
      </div>
    } @else if (error || !vehicle) {
      <p class="text-center py-24 text-brick">{{ error || 'Vehicle not found.' }}</p>
    } @else {
      <div class="max-w-5xl mx-auto px-6 py-12 grid md:grid-cols-5 gap-10">
        <div class="md:col-span-2">
          <div class="bg-white border border-mist rounded-sign p-8 flex flex-col items-center text-center">
            @if (images.length > 0) {
              <img
                [src]="'data:image/jpeg;base64,' + images[activeImageIndex].imageBase64"
                [alt]="vehicle.make + ' ' + vehicle.model"
                class="w-full h-48 object-cover rounded-sign mb-3 bg-mist"
              />
              @if (images.length > 1) {
                <div class="flex gap-2 mb-4">
                  @for (img of images; track img.id; let i = $index) {
                    <button
                      (click)="activeImageIndex = i"
                      class="w-12 h-12 rounded-sign overflow-hidden border-2"
                      [ngClass]="i === activeImageIndex ? 'border-marigold' : 'border-transparent'"
                    >
                      <img [src]="'data:image/jpeg;base64,' + img.imageBase64" alt="" class="w-full h-full object-cover" />
                    </button>
                  }
                </div>
              }
            } @else {
              <app-vehicle-glyph [category]="categoryName" className="w-28 h-28 mb-4"></app-vehicle-glyph>
            }
            <h1 class="font-display text-2xl font-bold text-ink">{{ vehicle.make }} {{ vehicle.model }}</h1>
            <p class="text-charcoal mb-3">{{ categoryName }} · {{ vehicle.year }}</p>
            <app-plate-badge [plate]="vehicle.plateNumber"></app-plate-badge>

            <dl class="w-full mt-6 text-sm text-left space-y-2">
              <div class="flex justify-between">
                <dt class="text-charcoal">Fuel</dt>
                <dd class="font-medium">{{ vehicle.fuelType }} · {{ vehicle.fuelConsumptionPerKm }}L/km</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-charcoal">Transmission</dt>
                <dd class="font-medium">{{ vehicle.transmissionType }}</dd>
              </div>
              @if (vehicle.seatingCapacity) {
                <div class="flex justify-between">
                  <dt class="text-charcoal">Seats</dt>
                  <dd class="font-medium">{{ vehicle.seatingCapacity }}</dd>
                </div>
              }
              <div class="flex justify-between">
                <dt class="text-charcoal">Location</dt>
                <dd class="font-medium">{{ vehicle.location }}</dd>
              </div>
              <div class="flex justify-between pt-2 border-t border-mist">
                <dt class="text-charcoal">Daily rate</dt>
                <dd class="font-display font-semibold text-ink">{{ formatUGX(vehicle.dailyRate) }}</dd>
              </div>
            </dl>
          </div>
        </div>

        <div class="md:col-span-3">
          @if (!token()) {
            <app-alert type="info" message="Log in to book this vehicle."></app-alert>
            <a routerLink="/login" class="underline font-medium">Log in</a>
          }

          @if (token() && user()?.roleCode !== 'CUSTOMER') {
            <app-alert type="info" message="Only customer accounts can book vehicles."></app-alert>
          }

          @if (token() && user()?.roleCode === 'CUSTOMER') {
            <ol class="flex items-center gap-3 mb-8 text-sm">
              @for (label of steps; track label; let i = $index) {
                <li class="flex items-center gap-2">
                  <span
                    class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold"
                    [ngClass]="step >= i + 1 ? 'bg-ink text-paper' : 'bg-mist text-charcoal'"
                  >
                    {{ i + 1 }}
                  </span>
                  <span [ngClass]="step >= i + 1 ? 'text-ink font-medium' : 'text-charcoal'">{{ label }}</span>
                  @if (i < steps.length - 1) {
                    <span class="w-6 h-px bg-mist mx-1"></span>
                  }
                </li>
              }
            </ol>

            @if (step === 1) {
              <app-card className="p-6 space-y-4">
                <div class="grid sm:grid-cols-2 gap-4">
                  <app-input label="Pickup location name" name="pickupLocation" [(ngModel)]="form.pickupLocation"></app-input>
                  <app-input label="Dropoff location name" name="dropoffLocation" [(ngModel)]="form.dropoffLocation"></app-input>
                </div>

                <app-location-picker
                  [pickup]="pickup"
                  [dropoff]="dropoff"
                  [(mode)]="mapMode"
                  (changed)="handleLocationChange($event)"
                ></app-location-picker>
                @if (liveDistanceKm() != null) {
                  <p class="text-sm font-medium text-ink">Straight-line distance: {{ liveDistanceKm()!.toFixed(1) }} km</p>
                }

                <div class="grid sm:grid-cols-2 gap-4">
                  <app-input label="Start" type="datetime-local" name="startDate" [(ngModel)]="form.startDate"></app-input>
                  <app-input label="End" type="datetime-local" name="endDate" [(ngModel)]="form.endDate"></app-input>
                </div>
                <p class="text-xs text-charcoal -mt-2">Pick both a date and a time for each - the field stays empty until both are set.</p>

                <label class="flex items-center gap-2 text-sm text-charcoal">
                  <input type="checkbox" [checked]="form.withDriver" (change)="toggleWithDriver($event)" />
                  I need a platform driver
                </label>
                @if (form.withDriver) {
                  <app-select label="Choose your driver" name="driverId" [(ngModel)]="form.driverId">
                    <option value="">Select an available driver…</option>
                    @for (d of availableDrivers; track d.id) {
                      <option [value]="d.id">
                        Driver #{{ d.id }} · Licence {{ d.licenceNumber }} ({{ d.licenceClass }}) · {{ d.yearsOfExperience ?? 0 }} yrs experience
                      </option>
                    }
                  </app-select>
                }
                @if (form.withDriver && availableDrivers.length === 0) {
                  <p class="text-xs text-brick">No drivers are currently available - try again shortly or book without a driver.</p>
                }
                <app-input label="Coupon code (optional)" name="couponCode" [(ngModel)]="form.couponCode"></app-input>

                <app-alert [message]="formError"></app-alert>

                <app-button variant="accent" [fullWidth]="true" [disabled]="submitting" (clicked)="handlePreview()">
                  @if (submitting) { <app-spinner></app-spinner> } @else { Preview cost }
                </app-button>
              </app-card>
            }

            @if (step === 2 && preview) {
              <app-card className="p-6 space-y-4">
                <h3 class="font-display font-semibold text-lg">Cost breakdown</h3>
                <dl class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <dt class="text-charcoal">Distance</dt>
                    <dd>{{ Number(preview.estimated_distance_km).toFixed(1) }} km</dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-charcoal">Duration</dt>
                    <dd>{{ preview.estimated_duration_hours }} hours</dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-charcoal">With driver</dt>
                    <dd>{{ preview.with_driver ? 'Yes' : 'No' }}</dd>
                  </div>
                  @if (preview.discount_percentage != null) {
                    <div class="flex justify-between text-savanna">
                      <dt>Discount applied</dt>
                      <dd>{{ preview.discount_percentage }}%</dd>
                    </div>
                  }
                  <div class="flex justify-between pt-2 border-t border-mist font-display font-semibold text-base">
                    <dt>Total</dt>
                    <dd>{{ formatUGX(preview.final_cost) }}</dd>
                  </div>
                </dl>

                <app-alert [message]="formError"></app-alert>

                <div class="flex gap-3">
                  <app-button variant="outline" (clicked)="step = 1">Back</app-button>
                  <app-button variant="accent" [fullWidth]="true" [disabled]="submitting" (clicked)="handleConfirm()">
                    @if (submitting) { <app-spinner></app-spinner> } @else { Confirm booking }
                  </app-button>
                </div>
              </app-card>
            }

            @if (step === 3 && bookingResult) {
              <app-card className="p-8 text-center">
                <h3 class="font-display text-xl font-semibold text-ink mb-2">Booking requested</h3>
                <p class="text-charcoal mb-6">
                  Booking #{{ bookingResult.id }} is ready. Head to My Bookings to pay and pick up the vehicle.
                </p>
                <app-button variant="accent" (clicked)="router.navigateByUrl('/my-bookings')">View my bookings</app-button>
              </app-card>
            }
          }
        </div>
      </div>
    }
  `,
})
export class VehicleDetailComponent implements OnInit {
  vehicle: any = null;
  images: any[] = [];
  activeImageIndex = 0;
  category: any = null;
  loading = true;
  error = '';

  step = 1;
  steps = STEPS;

  form = {
    withDriver: false,
    driverId: '',
    pickupLocation: '',
    dropoffLocation: '',
    startDate: '',
    endDate: '',
    couponCode: '',
  };
  availableDrivers: any[] = [];
  pickup: LatLngPoint | null = null;
  dropoff: LatLngPoint | null = null;
  mapMode: 'pickup' | 'dropoff' = 'pickup';

  preview: any = null;
  submitting = false;
  bookingResult: any = null;
  formError = '';

  Number = Number;
  formatUGX = formatUGX;

  token = this.auth.token;
  user = this.auth.user;

  private id!: number;

  constructor(private route: ActivatedRoute, public router: Router, private api: ApiService, private auth: AuthService) {}

  get categoryName() {
    return this.category?.categoryName;
  }

  async ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    const defaultStart = new Date();
    defaultStart.setDate(defaultStart.getDate() + 1);
    defaultStart.setHours(9, 0, 0, 0);
    const defaultEnd = new Date(defaultStart);
    defaultEnd.setDate(defaultEnd.getDate() + 1);
    this.form.startDate = formatDateTimeLocalInputValue(defaultStart);
    this.form.endDate = formatDateTimeLocalInputValue(defaultEnd);

    try {
      const v = await this.api.call<any>('VehicleService', 'getOne', { id: this.id });
      this.vehicle = v;
      const categories = await this.api.call<any[]>('VehicleCategoryService', 'listAll');
      this.category = categories.find((c) => c.id === v.categoryId);
      const imageList = await this.api.call<any[]>('VehicleService', 'listImages', { vehicle_id: this.id });
      this.images = imageList || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  liveDistanceKm(): number | null {
    if (this.pickup && this.dropoff) {
      return haversineKm(this.pickup.lat, this.pickup.lng, this.dropoff.lat, this.dropoff.lng);
    }
    return null;
  }

  handleLocationChange(next: { pickup: LatLngPoint | null; dropoff: LatLngPoint | null }) {
    this.pickup = next.pickup;
    this.dropoff = next.dropoff;
    if (next.pickup?.address) this.form.pickupLocation = next.pickup.address;
    if (next.dropoff?.address) this.form.dropoffLocation = next.dropoff.address;
  }

  async toggleWithDriver(e: Event) {
    const checked = (e.target as HTMLInputElement).checked;
    this.form.withDriver = checked;
    this.form.driverId = '';
    if (checked && this.token()) {
      try {
        const drivers = await this.api.call<any[]>('DriverProfileService', 'listAvailable', {}, this.token());
        this.availableDrivers = drivers || [];
      } catch {
        this.availableDrivers = [];
      }
    }
  }

  async handlePreview() {
    this.formError = '';
    if (!this.form.pickupLocation || !this.form.dropoffLocation || !this.form.startDate || !this.form.endDate) {
      this.formError = 'Please fill in every field before continuing.';
      return;
    }
    if (!this.pickup || !this.dropoff) {
      this.formError = 'Please set both a pickup and a dropoff point on the map.';
      return;
    }
    if (this.form.withDriver && !this.form.driverId) {
      this.formError = 'Please pick a driver from the list before continuing.';
      return;
    }
    this.submitting = true;
    try {
      const result = await this.api.call<any>('BookingService', 'previewCost', {
        vehicle_id: this.id,
        with_driver: this.form.withDriver,
        pickup_lat: this.pickup.lat,
        pickup_lng: this.pickup.lng,
        dropoff_lat: this.dropoff.lat,
        dropoff_lng: this.dropoff.lng,
        start_date: toEpochMillis(this.form.startDate),
        end_date: toEpochMillis(this.form.endDate),
        coupon_code: this.form.couponCode || undefined,
      });
      this.preview = result;
      this.step = 2;
    } catch (err: any) {
      this.formError = err.message;
    } finally {
      this.submitting = false;
    }
  }

  async handleConfirm() {
    this.submitting = true;
    this.formError = '';
    try {
      const result = await this.api.call<any>(
        'BookingService',
        'createBooking',
        {
          vehicle_id: this.id,
          with_driver: this.form.withDriver,
          driver_id: this.form.withDriver ? Number(this.form.driverId) : undefined,
          pickup_location: this.form.pickupLocation,
          dropoff_location: this.form.dropoffLocation,
          pickup_lat: this.pickup!.lat,
          pickup_lng: this.pickup!.lng,
          dropoff_lat: this.dropoff!.lat,
          dropoff_lng: this.dropoff!.lng,
          start_date: toEpochMillis(this.form.startDate),
          end_date: toEpochMillis(this.form.endDate),
          coupon_code: this.form.couponCode || undefined,
        },
        this.token()
      );
      this.bookingResult = result;
      this.step = 3;
    } catch (err: any) {
      this.formError = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
