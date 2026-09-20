import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { InputComponent } from '../../shared/input.component';
import { SelectComponent } from '../../shared/select.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { ModalComponent } from '../../shared/modal.component';
import { OwnerVehicleCardComponent } from './owner-vehicle-card.component';

const FUEL_TYPES = ['PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID'];
const TRANSMISSIONS = ['MANUAL', 'AUTOMATIC'];
const MIN_IMAGES = 2;
const MAX_IMAGES = 4;

const EMPTY_FORM = {
  plate_number: '', make: '', model: '', year: '', category_id: '',
  fuel_type: 'PETROL', fuel_capacity_litres: '', fuel_consumption_per_km: '',
  transmission_type: 'MANUAL', seating_capacity: '', daily_rate: '', location: '',
};

function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve((reader.result as string).split(',')[1]);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

@Component({
  selector: 'app-my-vehicles',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InputComponent,
    SelectComponent,
    ButtonComponent,
    AlertComponent,
    SpinnerComponent,
    EmptyStateComponent,
    ModalComponent,
    OwnerVehicleCardComponent,
  ],
  template: `
    <div class="max-w-4xl mx-auto px-6 py-12">
      <div class="flex items-center justify-between mb-8">
        <h1 class="font-display text-2xl font-bold">My vehicles</h1>
        <app-button variant="accent" (clicked)="showForm = true">Submit a vehicle</app-button>
      </div>

      @if (loading) {
        <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
      }

      <app-alert className="mb-4" [message]="error"></app-alert>

      @if (!loading && vehicles.length === 0) {
        <app-empty-state title="No vehicles submitted yet" description="List your car, truck, or tractor and start earning when it's rented.">
          <app-button variant="accent" (clicked)="showForm = true">Submit a vehicle</app-button>
        </app-empty-state>
      }

      <div class="grid sm:grid-cols-2 gap-5">
        @for (v of vehicles; track v.id) {
          <app-owner-vehicle-card [vehicle]="v" [categoryName]="categoryName(v.categoryId)" [token]="token()" (photoAdded)="load()"></app-owner-vehicle-card>
        }
      </div>

      <app-modal [open]="showForm" title="Submit a vehicle" (close)="closeForm()">
        <form (submit)="handleSubmit($event)" class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <app-input label="Plate number" name="plate_number" [(ngModel)]="form.plate_number" [required]="true"></app-input>
            <app-input label="Year" type="number" name="year" [(ngModel)]="form.year"></app-input>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <app-input label="Make" name="make" [(ngModel)]="form.make" [required]="true"></app-input>
            <app-input label="Model" name="model" [(ngModel)]="form.model" [required]="true"></app-input>
          </div>
          <app-select label="Category" name="category_id" [(ngModel)]="form.category_id" [required]="true">
            <option value="">Select a category</option>
            @for (c of categories; track c.id) { <option [value]="c.id">{{ c.categoryName }}</option> }
          </app-select>
          <div class="grid grid-cols-2 gap-4">
            <app-select label="Fuel type" name="fuel_type" [(ngModel)]="form.fuel_type">
              @for (f of fuelTypes; track f) { <option [value]="f">{{ f }}</option> }
            </app-select>
            <app-input label="Tank capacity (L)" type="number" name="fuel_capacity_litres" [(ngModel)]="form.fuel_capacity_litres"></app-input>
          </div>
          <app-input
            label="Fuel consumption (litres per km)"
            type="number"
            step="0.01"
            placeholder="e.g. 0.08 for 8L/100km"
            name="fuel_consumption_per_km"
            [(ngModel)]="form.fuel_consumption_per_km"
            [required]="true"
          ></app-input>
          <div class="grid grid-cols-2 gap-4">
            <app-select label="Transmission" name="transmission_type" [(ngModel)]="form.transmission_type">
              @for (t of transmissions; track t) { <option [value]="t">{{ t }}</option> }
            </app-select>
            <app-input label="Seats" type="number" name="seating_capacity" [(ngModel)]="form.seating_capacity"></app-input>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <app-input label="Daily rate (UGX)" type="number" name="daily_rate" [(ngModel)]="form.daily_rate"></app-input>
            <app-input label="Location" name="location" [(ngModel)]="form.location"></app-input>
          </div>

          <label class="block">
            <span class="block text-sm font-medium text-charcoal mb-1">Photos ({{ minImages }}-{{ maxImages }} required)</span>
            <input type="file" accept="image/*" multiple (change)="handleImageSelect($event)" class="w-full text-sm" />
            @if (imageFiles.length > 0) {
              <div class="flex gap-2 mt-2">
                @for (f of imageFiles; track f.name) {
                  <span class="text-xs bg-mist px-2 py-1 rounded-sign truncate max-w-[100px]">{{ f.name }}</span>
                }
              </div>
            }
          </label>

          <app-alert [message]="formError"></app-alert>
          <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="submitting">
            @if (submitting) { <app-spinner></app-spinner> } @else { Submit for verification }
          </app-button>
        </form>
      </app-modal>
    </div>
  `,
})
export class MyVehiclesComponent implements OnInit {
  vehicles: any[] = [];
  categories: any[] = [];
  loading = true;
  error = '';
  showForm = false;
  form = { ...EMPTY_FORM };
  imageFiles: File[] = [];
  submitting = false;
  formError = '';

  fuelTypes = FUEL_TYPES;
  transmissions = TRANSMISSIONS;
  minImages = MIN_IMAGES;
  maxImages = MAX_IMAGES;

  token = this.auth.token;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const [vehicleList, categoryList] = await Promise.all([
        this.api.call<any[]>('VehicleService', 'myVehicles', {}, this.token()),
        this.api.call<any[]>('VehicleCategoryService', 'listAll'),
      ]);
      this.vehicles = vehicleList || [];
      this.categories = categoryList || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  categoryName(id: number) {
    return this.categories.find((c) => c.id === id)?.categoryName;
  }

  handleImageSelect(e: Event) {
    const input = e.target as HTMLInputElement;
    this.imageFiles = Array.from(input.files || []).slice(0, MAX_IMAGES);
  }

  closeForm() {
    this.showForm = false;
    this.form = { ...EMPTY_FORM };
    this.imageFiles = [];
    this.formError = '';
  }

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.formError = '';

    if (this.imageFiles.length < MIN_IMAGES) {
      this.formError = `Please add at least ${MIN_IMAGES} photos (up to ${MAX_IMAGES}).`;
      return;
    }

    this.submitting = true;
    try {
      const vehicle = await this.api.call<any>(
        'VehicleService',
        'submitOwnerVehicle',
        {
          plate_number: this.form.plate_number,
          make: this.form.make,
          model: this.form.model,
          year: Number(this.form.year) || undefined,
          category_id: Number(this.form.category_id),
          fuel_type: this.form.fuel_type,
          fuel_capacity_litres: Number(this.form.fuel_capacity_litres) || undefined,
          fuel_consumption_per_km: Number(this.form.fuel_consumption_per_km),
          transmission_type: this.form.transmission_type,
          seating_capacity: Number(this.form.seating_capacity) || undefined,
          daily_rate: Number(this.form.daily_rate) || undefined,
          location: this.form.location,
        },
        this.token()
      );

      const base64Images = await Promise.all(this.imageFiles.map(fileToBase64));
      await this.api.call('VehicleService', 'addImages', { vehicle_id: vehicle.id, images: base64Images }, this.token());

      this.closeForm();
      await this.load();
    } catch (err: any) {
      this.formError = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
