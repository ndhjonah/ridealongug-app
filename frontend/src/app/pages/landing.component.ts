import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../core/api.service';
import { VehicleCardComponent } from '../shared/vehicle-card.component';
import { EmptyStateComponent } from '../shared/empty-state.component';
import { SpinnerComponent } from '../shared/spinner.component';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, VehicleCardComponent, EmptyStateComponent, SpinnerComponent],
  template: `
    <div>
      <section class="max-w-6xl mx-auto px-6 pt-16 pb-12 grid md:grid-cols-2 gap-10 items-center">
        <div>
          <h1 class="font-display text-4xl sm:text-5xl font-bold leading-tight text-ink">
            Every road in Uganda,<br />one verified fleet.
          </h1>
          <p class="mt-5 text-charcoal text-lg max-w-md">
            Sedans for the city, trucks for the harvest, tractors for the field — every vehicle
            inspected, insured, and serviced before it's listed.
          </p>
        </div>
        <div class="relative h-56 sm:h-72">
          <svg viewBox="0 0 400 200" class="w-full h-full">
            <path d="M0 170 Q 100 100 200 140 T 400 90" stroke="#14161A" stroke-width="3" fill="none" stroke-dasharray="2 10" stroke-linecap="round" />
            <circle cx="0" cy="170" r="6" fill="#F2B705" />
            <circle cx="200" cy="140" r="6" fill="#2F6D4F" />
            <circle cx="400" cy="90" r="6" fill="#B33F2E" />
          </svg>
        </div>
      </section>

      <section class="max-w-6xl mx-auto px-6 pb-20">
        <div class="flex items-center gap-2 mb-8 flex-wrap">
          @for (cat of categoryOptions(); track cat) {
            <button
              (click)="activeCategory = cat"
              class="px-4 py-2 rounded-sign text-sm font-medium transition-colors"
              [ngClass]="activeCategory === cat ? 'bg-ink text-paper' : 'bg-white border border-mist text-charcoal hover:border-ink'"
            >
              {{ cat }}
            </button>
          }
        </div>

        @if (loading) {
          <div class="flex justify-center py-20 text-charcoal">
            <app-spinner className="w-6 h-6"></app-spinner>
          </div>
        }

        @if (error) {
          <p class="text-brick text-center py-10">{{ error }}</p>
        }

        @if (!loading && !error && filtered().length === 0) {
          <app-empty-state
            title="No vehicles available right now"
            description="New listings appear here as soon as they pass inspection."
          ></app-empty-state>
        }

        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          @for (v of filtered(); track v.id) {
            <app-vehicle-card [vehicle]="v" [categoryName]="categoryMap[v.categoryId]"></app-vehicle-card>
          }
        </div>
      </section>
    </div>
  `,
})
export class LandingComponent implements OnInit {
  vehicles: any[] = [];
  categories: any[] = [];
  activeCategory = 'All';
  loading = true;
  error = '';
  categoryMap: Record<number, string> = {};

  constructor(private api: ApiService) {}

  async ngOnInit() {
    try {
      const [vehicleList, categoryList] = await Promise.all([
        this.api.call<any[]>('VehicleService', 'browseAvailable'),
        this.api.call<any[]>('VehicleCategoryService', 'listAll'),
      ]);
      this.vehicles = vehicleList || [];
      this.categories = categoryList || [];
      this.categoryMap = {};
      this.categories.forEach((c) => (this.categoryMap[c.id] = c.categoryName));
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  categoryOptions() {
    return ['All', ...this.categories.map((c) => c.categoryName)];
  }

  filtered() {
    if (this.activeCategory === 'All') return this.vehicles;
    return this.vehicles.filter((v) => this.categoryMap[v.categoryId] === this.activeCategory);
  }
}
