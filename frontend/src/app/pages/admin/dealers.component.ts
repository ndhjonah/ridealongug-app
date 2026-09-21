import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, searchPayload } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CardComponent } from '../../shared/card.component';
import { InputComponent } from '../../shared/input.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';
import { ModalComponent } from '../../shared/modal.component';

const EMPTY_FORM = { dealer_name: '', contact_person: '', phone_number: '', email: '', location: '', bond_registration_number: '' };

@Component({
  selector: 'app-dealers',
  standalone: true,
  imports: [CommonModule, FormsModule, CardComponent, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent, EmptyStateComponent, ModalComponent],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <div class="flex justify-end mb-4">
          <app-button variant="accent" (clicked)="showForm = true">Add dealer</app-button>
        </div>

        <app-alert className="mb-4" [message]="error"></app-alert>

        @if (dealers.length === 0) {
          <app-empty-state title="No dealers on file" description="Add a car bond manually, or wait for owners to self-register."></app-empty-state>
        } @else {
          <div class="grid sm:grid-cols-2 gap-4">
            @for (d of dealers; track d.id) {
              <app-card className="p-5">
                <p class="font-display font-semibold">{{ d.dealerName }}</p>
                <p class="text-sm text-charcoal">{{ d.contactPerson }} · {{ d.phoneNumber }}</p>
                <p class="text-sm text-charcoal">{{ d.location }}</p>
                @if (d.userId) { <p class="text-xs text-savanna mt-1">Self-registered account</p> }
              </app-card>
            }
          </div>
        }

        <app-modal [open]="showForm" title="Add a dealer" (close)="showForm = false">
          <form (submit)="handleSubmit($event)" class="space-y-4">
            <app-input label="Dealer name" name="dealer_name" [(ngModel)]="form.dealer_name" [required]="true"></app-input>
            <app-input label="Contact person" name="contact_person" [(ngModel)]="form.contact_person" [required]="true"></app-input>
            <app-input label="Phone number" name="phone_number" [(ngModel)]="form.phone_number" [required]="true"></app-input>
            <app-input label="Email" name="email" [(ngModel)]="form.email"></app-input>
            <app-input label="Location" name="location" [(ngModel)]="form.location"></app-input>
            <app-input label="Bond registration number" name="bond_registration_number" [(ngModel)]="form.bond_registration_number"></app-input>
            <app-alert [message]="error"></app-alert>
            <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="submitting">
              @if (submitting) { <app-spinner></app-spinner> } @else { Add dealer }
            </app-button>
          </form>
        </app-modal>
      </div>
    }
  `,
})
export class DealersComponent implements OnInit {
  dealers: any[] = [];
  loading = true;
  error = '';
  showForm = false;
  form = { ...EMPTY_FORM };
  submitting = false;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const result = await this.api.call<any>('DealerService', 'search', searchPayload(), this.auth.token());
      this.dealers = result?.content || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.submitting = true;
    this.error = '';
    try {
      await this.api.call('DealerService', 'createDealer', this.form, this.auth.token());
      this.showForm = false;
      this.form = { ...EMPTY_FORM };
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
