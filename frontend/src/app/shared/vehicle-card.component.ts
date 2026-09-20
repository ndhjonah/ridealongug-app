import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { categoryColor } from '../utils/vehicle-glyphs';
import { formatUGX } from '../utils/format';
import { PlateBadgeComponent } from './plate-badge.component';
import { VehiclePhotoComponent } from './vehicle-photo.component';

@Component({
  selector: 'app-vehicle-card',
  standalone: true,
  imports: [RouterLink, PlateBadgeComponent, VehiclePhotoComponent],
  template: `
    <a
      [routerLink]="['/vehicles', vehicle.id]"
      class="block bg-white border border-mist rounded-sign overflow-hidden hover:shadow-md transition-shadow group"
    >
      <div [style.backgroundColor]="color().bar" class="h-1.5 w-full"></div>
      <app-vehicle-photo [vehicleId]="vehicle.id" [category]="categoryName" className="w-full h-40 rounded-none"></app-vehicle-photo>
      <div class="p-5">
        <div class="flex items-start justify-between mb-3">
          <h3 class="font-display font-semibold text-lg text-ink group-hover:underline">
            {{ vehicle.make }} {{ vehicle.model }}
          </h3>
          <app-plate-badge [plate]="vehicle.plateNumber"></app-plate-badge>
        </div>
        <p class="text-sm text-charcoal mb-3">{{ categoryName || 'Vehicle' }} · {{ vehicle.year || '' }}</p>
        <div class="flex items-center justify-between">
          <span class="text-sm text-charcoal">{{ vehicle.location }}</span>
          <span class="font-display font-semibold text-ink">{{ formatUGX(vehicle.dailyRate) }}/day</span>
        </div>
      </div>
    </a>
  `,
})
export class VehicleCardComponent {
  @Input() vehicle: any;
  @Input() categoryName: string | null | undefined = null;

  formatUGX = formatUGX;

  color() {
    return categoryColor(this.categoryName);
  }
}
