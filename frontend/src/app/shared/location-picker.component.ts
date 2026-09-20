import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';

const DEFAULT_CENTER: [number, number] = [0.3476, 32.5825];

export interface LatLngPoint {
  lat: number;
  lng: number;
  address: string | null;
}

async function reverseGeocode(lat: number, lng: number): Promise<string> {
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 6000);
    const response = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}&zoom=17&addressdetails=0`,
      { headers: { Accept: 'application/json' }, signal: controller.signal }
    );
    clearTimeout(timeout);
    if (!response.ok) throw new Error('lookup failed');
    const data = await response.json();
    return data?.display_name || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
  } catch {
    return `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
  }
}

const pickupIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  className: 'hue-rotate-[100deg]',
});

const dropoffIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  className: 'hue-rotate-[300deg]',
});

@Component({
  selector: 'app-location-picker',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <div class="flex gap-2 mb-2">
        <button
          type="button"
          (click)="mode = 'pickup'; modeChange.emit(mode)"
          class="px-3 py-1.5 rounded-sign text-xs font-semibold transition-colors"
          [ngClass]="mode === 'pickup' ? 'bg-savanna text-white' : 'bg-mist text-charcoal'"
        >
          Set pickup
        </button>
        <button
          type="button"
          (click)="mode = 'dropoff'; modeChange.emit(mode)"
          class="px-3 py-1.5 rounded-sign text-xs font-semibold transition-colors"
          [ngClass]="mode === 'dropoff' ? 'bg-brick text-white' : 'bg-mist text-charcoal'"
        >
          Set dropoff
        </button>
        @if (geocoding) {
          <span class="text-xs text-charcoal self-center">Looking up address...</span>
        }
      </div>
      <div class="rounded-sign overflow-hidden border border-mist" style="height: 260px">
        <div #mapEl style="height: 100%; width: 100%"></div>
      </div>
      <p class="text-xs text-charcoal mt-2">
        Click the map to place the {{ mode }} marker - the address field above fills itself in automatically.
        Switch modes above to set the other point.
      </p>
    </div>
  `,
})
export class LocationPickerComponent implements OnChanges, AfterViewInit, OnDestroy {
  @Input() pickup: LatLngPoint | null = null;
  @Input() dropoff: LatLngPoint | null = null;
  @Input() mode: 'pickup' | 'dropoff' = 'pickup';
  @Output() modeChange = new EventEmitter<'pickup' | 'dropoff'>();
  @Output() changed = new EventEmitter<{ pickup: LatLngPoint | null; dropoff: LatLngPoint | null }>();

  @ViewChild('mapEl') mapEl!: ElementRef<HTMLDivElement>;

  geocoding: 'pickup' | 'dropoff' | null = null;
  private map?: L.Map;
  private pickupMarker?: L.Marker;
  private dropoffMarker?: L.Marker;
  private line?: L.Polyline;
  private initialized = false;

  ngOnChanges() {
    this.redraw();
  }

  ngAfterViewInit() {
    this.initMap();
  }

  ngOnDestroy() {
    this.map?.remove();
  }

  private initMap() {
    const center = this.pickup || this.dropoff || { lat: DEFAULT_CENTER[0], lng: DEFAULT_CENTER[1] };
    this.map = L.map(this.mapEl.nativeElement).setView([center.lat, center.lng], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(this.map);
    this.map.on('click', (e: L.LeafletMouseEvent) => this.handlePick(this.mode, e.latlng.lat, e.latlng.lng));
    this.initialized = true;
  }

  private async handlePick(pickMode: 'pickup' | 'dropoff', lat: number, lng: number) {
    const point: LatLngPoint = { lat, lng, address: null };
    if (pickMode === 'pickup') {
      this.pickup = point;
      this.changed.emit({ pickup: this.pickup, dropoff: this.dropoff });
    } else {
      this.dropoff = point;
      this.changed.emit({ pickup: this.pickup, dropoff: this.dropoff });
    }
    this.redraw();

    this.geocoding = pickMode;
    const address = await reverseGeocode(lat, lng);
    if (this.geocoding === pickMode) this.geocoding = null;

    if (pickMode === 'pickup') {
      this.pickup = { lat, lng, address };
    } else {
      this.dropoff = { lat, lng, address };
    }
    this.changed.emit({ pickup: this.pickup, dropoff: this.dropoff });
  }

  private redraw() {
    if (!this.map) return;

    if (this.pickupMarker) {
      this.map.removeLayer(this.pickupMarker);
      this.pickupMarker = undefined;
    }
    if (this.dropoffMarker) {
      this.map.removeLayer(this.dropoffMarker);
      this.dropoffMarker = undefined;
    }
    if (this.line) {
      this.map.removeLayer(this.line);
      this.line = undefined;
    }

    if (this.pickup) {
      this.pickupMarker = L.marker([this.pickup.lat, this.pickup.lng], { icon: pickupIcon }).addTo(this.map);
    }
    if (this.dropoff) {
      this.dropoffMarker = L.marker([this.dropoff.lat, this.dropoff.lng], { icon: dropoffIcon }).addTo(this.map);
    }
    if (this.pickup && this.dropoff) {
      this.line = L.polyline(
        [
          [this.pickup.lat, this.pickup.lng],
          [this.dropoff.lat, this.dropoff.lng],
        ],
        { color: '#14161A', dashArray: '4 8' }
      ).addTo(this.map);
    }
  }
}
