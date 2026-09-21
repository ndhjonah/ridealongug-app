import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ModalComponent } from '../../shared/modal.component';
import { SelectComponent } from '../../shared/select.component';
import { InputComponent } from '../../shared/input.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';

@Component({
  selector: 'app-inspect-modal',
  standalone: true,
  imports: [FormsModule, ModalComponent, SelectComponent, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent],
  template: `
    <app-modal [open]="true" [title]="'Verify ' + vehicle.make + ' ' + vehicle.model" (close)="closed.emit()">
      <div class="space-y-6">
        <div>
          <h4 class="font-semibold text-sm mb-2">1. Mechanical inspection</h4>
          <div class="space-y-3">
            <app-select name="mechanical_condition" [(ngModel)]="inspection.mechanical_condition">
              <option value="GOOD">Good</option>
              <option value="NEEDS_REPAIR">Needs repair</option>
              <option value="FAILED">Failed</option>
            </app-select>
            <app-input placeholder="Notes" name="notes" [(ngModel)]="inspection.notes"></app-input>
          </div>
        </div>

        <div>
          <h4 class="font-semibold text-sm mb-2">2. Insurance (optional here, required before it's bookable)</h4>
          <div class="grid grid-cols-2 gap-3">
            <app-input placeholder="Provider" name="insurance_provider" [(ngModel)]="insurance.insurance_provider"></app-input>
            <app-input placeholder="Policy number" name="policy_number" [(ngModel)]="insurance.policy_number"></app-input>
            <app-input type="date" name="coverage_start_date" [(ngModel)]="insurance.coverage_start_date"></app-input>
            <app-input type="date" name="coverage_expiry_date" [(ngModel)]="insurance.coverage_expiry_date"></app-input>
          </div>
        </div>

        <div>
          <h4 class="font-semibold text-sm mb-2">3. Service record (optional here)</h4>
          <div class="grid grid-cols-2 gap-3">
            <app-input type="number" placeholder="Current mileage" name="serviced_at_mileage" [(ngModel)]="service.serviced_at_mileage"></app-input>
            <app-input type="number" placeholder="Next due mileage" name="next_service_due_mileage" [(ngModel)]="service.next_service_due_mileage"></app-input>
            <app-input type="date" name="service_date" [(ngModel)]="service.service_date"></app-input>
          </div>
        </div>

        <app-alert [message]="error"></app-alert>

        <app-button variant="accent" [fullWidth]="true" [disabled]="submitting" (clicked)="submitAll()">
          @if (submitting) { <app-spinner></app-spinner> } @else { Save verification }
        </app-button>
      </div>
    </app-modal>
  `,
})
export class InspectModalComponent {
  @Input() vehicle: any;
  @Output() closed = new EventEmitter<void>();
  @Output() done = new EventEmitter<void>();

  error = '';
  submitting = false;

  inspection = { mechanical_condition: 'GOOD', notes: '' };
  insurance = { insurance_provider: '', policy_number: '', insurance_type: 'THIRD_PARTY', coverage_start_date: '', coverage_expiry_date: '' };
  service = { serviced_at_mileage: '', service_date: '', service_type: 'Full service', next_service_due_mileage: '' };

  constructor(private api: ApiService, private auth: AuthService) {}

  async submitAll() {
    this.error = '';
    this.submitting = true;
    try {
      await this.api.call(
        'VehicleService',
        'recordInspection',
        { vehicle_id: this.vehicle.id, mechanical_condition: this.inspection.mechanical_condition, notes: this.inspection.notes },
        this.auth.token()
      );

      if (this.insurance.insurance_provider && this.insurance.policy_number) {
        await this.api.call(
          'VehicleService',
          'addInsurance',
          {
            vehicle_id: this.vehicle.id,
            insurance_provider: this.insurance.insurance_provider,
            policy_number: this.insurance.policy_number,
            insurance_type: this.insurance.insurance_type,
            coverage_start_date: this.insurance.coverage_start_date,
            coverage_expiry_date: this.insurance.coverage_expiry_date,
          },
          this.auth.token()
        );
      }

      if (this.service.serviced_at_mileage && this.service.next_service_due_mileage) {
        await this.api.call(
          'VehicleService',
          'recordService',
          {
            vehicle_id: this.vehicle.id,
            serviced_at_mileage: Number(this.service.serviced_at_mileage),
            service_date: this.service.service_date,
            service_type: this.service.service_type,
            next_service_due_mileage: Number(this.service.next_service_due_mileage),
          },
          this.auth.token()
        );
      }

      this.done.emit();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
