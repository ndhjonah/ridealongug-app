import { Component, Input, OnChanges } from '@angular/core';
import { ApiService } from '../core/api.service';
import { VehicleGlyphComponent } from './vehicle-glyph.component';

@Component({
  selector: 'app-vehicle-photo',
  standalone: true,
  imports: [VehicleGlyphComponent],
  template: `
    @if (image === undefined) {
      <div class="{{ className }} bg-mist animate-pulse rounded-sign"></div>
    } @else if (image) {
      <img
        [src]="'data:image/jpeg;base64,' + image.imageBase64"
        [alt]="category || 'Vehicle photo'"
        class="{{ className }} object-cover rounded-sign bg-mist"
      />
    } @else {
      <div class="{{ className }} flex items-center justify-center bg-mist rounded-sign">
        <app-vehicle-glyph [category]="category" className="w-16 h-16"></app-vehicle-glyph>
      </div>
    }
  `,
})
export class VehiclePhotoComponent implements OnChanges {
  @Input() vehicleId!: number;
  @Input() category: string | null | undefined = null;
  @Input() className = 'w-full h-40';

  image: any = undefined;

  constructor(private api: ApiService) {}

  ngOnChanges() {
    this.image = undefined;
    this.api
      .call('VehicleService', 'listImages', { vehicle_id: this.vehicleId })
      .then((images: any[]) => {
        const primary = images?.find((i) => i.isPrimary) || images?.[0];
        this.image = primary || null;
      })
      .catch(() => {
        this.image = null;
      });
  }
}
