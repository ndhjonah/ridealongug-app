import { Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { ModalComponent } from './modal.component';

@Component({
  selector: 'app-route-map-modal',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  template: `
    <app-modal [open]="!!booking" [title]="'Route for booking #' + booking?.id" (close)="close.emit()">
      @if (booking && !hasCoordinates()) {
        <p class="text-charcoal text-sm">
          This booking has no map coordinates on file (it was likely created before location
          tracking was added).
        </p>
      }
      @if (booking && hasCoordinates()) {
        <div class="rounded-sign overflow-hidden border border-mist mb-3" style="height: 320px">
          <div #mapEl style="height: 100%; width: 100%"></div>
        </div>
        <div class="flex justify-between text-sm text-charcoal mb-1">
          <span>Pickup: {{ booking.pickupLocation }}</span>
          <span>Dropoff: {{ booking.dropoffLocation }}</span>
        </div>
        @if (booking.estimatedDistanceKm != null) {
          <p class="text-sm font-medium text-ink">
            Straight-line distance: {{ Number(booking.estimatedDistanceKm).toFixed(1) }} km
          </p>
        }
      }
    </app-modal>
  `,
})
export class RouteMapModalComponent implements OnChanges, OnDestroy {
  @Input() booking: any = null;
  @Output() close = new EventEmitter<void>();

  @ViewChild('mapEl') mapEl?: ElementRef<HTMLDivElement>;

  Number = Number;
  private map?: L.Map;

  hasCoordinates() {
    return this.booking?.pickupLat != null && this.booking?.dropoffLat != null;
  }

  ngOnChanges() {
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }
    if (this.booking && this.hasCoordinates()) {
      setTimeout(() => this.renderMap(), 0);
    }
  }

  ngOnDestroy() {
    this.map?.remove();
  }

  private renderMap() {
    if (!this.mapEl) return;
    const b = this.booking;
    const bounds = L.latLngBounds([b.pickupLat, b.pickupLng], [b.dropoffLat, b.dropoffLng]).pad(0.25);
    this.map = L.map(this.mapEl.nativeElement).fitBounds(bounds);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(this.map);
    L.marker([b.pickupLat, b.pickupLng]).addTo(this.map);
    L.marker([b.dropoffLat, b.dropoffLng]).addTo(this.map);
    L.polyline(
      [
        [b.pickupLat, b.pickupLng],
        [b.dropoffLat, b.dropoffLng],
      ],
      { color: '#14161A', dashArray: '4 8' }
    ).addTo(this.map);
  }
}
