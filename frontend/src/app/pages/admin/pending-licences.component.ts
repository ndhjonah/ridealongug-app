import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-pending-licences',
  standalone: true,
  imports: [CommonModule, CardComponent, ButtonComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>
        @if (licences.length === 0) {
          <app-empty-state title="No licences waiting for verification" description="New submissions will show up here."></app-empty-state>
        } @else {
          <div class="space-y-3">
            @for (l of licences; track l.id) {
              <app-card className="p-5 flex items-center justify-between">
                <div>
                  <p class="font-medium">{{ l.licenceNumber }} · Class {{ l.licenceClass }}</p>
                  <p class="text-sm text-charcoal">Expires {{ formatDate(l.expiryDate) }}</p>
                </div>
                <div class="flex gap-2">
                  <app-button variant="outline" [disabled]="busyId === l.id" (clicked)="verify(l.id, 'REJECTED')">Reject</app-button>
                  <app-button variant="accent" [disabled]="busyId === l.id" (clicked)="verify(l.id, 'VERIFIED')">
                    @if (busyId === l.id) { <app-spinner></app-spinner> } @else { Verify }
                  </app-button>
                </div>
              </app-card>
            }
          </div>
        }
      </div>
    }
  `,
})
export class PendingLicencesComponent implements OnInit {
  licences: any[] = [];
  loading = true;
  error = '';
  busyId: number | null = null;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const result = await this.api.call<any[]>('DrivingLicenceService', 'pendingVerification', {}, this.auth.token());
      this.licences = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  async verify(id: number, status: string) {
    this.busyId = id;
    try {
      const payload: Record<string, unknown> = { id, verification_status: status };
      if (status === 'REJECTED') {
        payload['rejection_reason'] = window.prompt('Reason for rejection?') || 'Not specified';
      }
      await this.api.call('DrivingLicenceService', 'verifyLicence', payload, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }
}
