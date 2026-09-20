import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { formatUGX } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { StatusPillComponent } from '../../shared/status-pill.component';
import { AlertComponent } from '../../shared/alert.component';
import { VehiclePhotoComponent } from '../../shared/vehicle-photo.component';

@Component({
  selector: 'app-owner-vehicle-card',
  standalone: true,
  imports: [CardComponent, StatusPillComponent, AlertComponent, VehiclePhotoComponent],
  template: `
    <app-card className="p-5">
      <div class="flex items-start justify-between mb-3">
        <app-vehicle-photo [vehicleId]="vehicle.id" [category]="categoryName" className="w-20 h-16"></app-vehicle-photo>
        <app-status-pill [status]="vehicle.mechanicalStatus"></app-status-pill>
      </div>
      <h3 class="font-display font-semibold">{{ vehicle.make }} {{ vehicle.model }}</h3>
      <p class="text-sm text-charcoal mb-2">{{ vehicle.plateNumber }} · {{ categoryName }}</p>
      <div class="flex justify-between text-sm mb-3">
        <span class="text-charcoal">{{ vehicle.isVisible ? 'Live for booking' : 'Not yet visible' }}</span>
        <span class="font-medium">{{ formatUGX(vehicle.dailyRate) }}/day</span>
      </div>
      <app-alert className="mb-2" [message]="error"></app-alert>
      <label class="text-sm text-ink underline cursor-pointer">
        {{ uploading ? 'Uploading...' : 'Add another photo' }}
        <input type="file" accept="image/*" (change)="handleFile($event)" class="hidden" [disabled]="uploading" />
      </label>
    </app-card>
  `,
})
export class OwnerVehicleCardComponent {
  @Input() vehicle: any;
  @Input() categoryName: string | null | undefined = null;
  @Input() token: string | null = null;
  @Output() photoAdded = new EventEmitter<void>();

  uploading = false;
  error = '';
  formatUGX = formatUGX;

  constructor(private api: ApiService) {}

  private fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve((reader.result as string).split(',')[1]);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  async handleFile(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.error = '';
    this.uploading = true;
    try {
      const base64 = await this.fileToBase64(file);
      await this.api.call('VehicleService', 'addImage', { vehicle_id: this.vehicle.id, image_base64: base64, is_primary: false }, this.token);
      this.photoAdded.emit();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.uploading = false;
      input.value = '';
    }
  }
}
