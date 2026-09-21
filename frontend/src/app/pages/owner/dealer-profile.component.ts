import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CardComponent } from '../../shared/card.component';
import { InputComponent } from '../../shared/input.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';

const EMPTY_FORM = { dealer_name: '', contact_person: '', phone_number: '', email: '', location: '', bond_registration_number: '' };

@Component({
  selector: 'app-dealer-profile',
  standalone: true,
  imports: [FormsModule, CardComponent, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent],
  template: `
    @if (dealer === undefined) {
      <div class="flex justify-center py-24">
        <app-spinner className="w-6 h-6 text-charcoal"></app-spinner>
      </div>
    } @else {
      <div class="max-w-lg mx-auto px-6 py-12">
        <h1 class="font-display text-2xl font-bold mb-2">Business profile</h1>
        <p class="text-charcoal mb-8">
          Register your car bond once, and every vehicle you submit is automatically listed under your business.
        </p>

        <app-alert className="mb-4" [message]="error"></app-alert>

        @if (dealer) {
          <app-card className="p-6 space-y-2">
            <p class="font-display font-semibold text-lg">{{ dealer.dealerName }}</p>
            <p class="text-sm text-charcoal">{{ dealer.contactPerson }} · {{ dealer.phoneNumber }}</p>
            <p class="text-sm text-charcoal">{{ dealer.location }}</p>
            @if (dealer.bondRegistrationNumber) {
              <p class="text-sm text-charcoal">Reg. no: {{ dealer.bondRegistrationNumber }}</p>
            }
          </app-card>
        } @else {
          <form (submit)="handleSubmit($event)" class="space-y-4">
            <app-input label="Business / bond name" name="dealer_name" [(ngModel)]="form.dealer_name" [required]="true"></app-input>
            <app-input label="Contact person" name="contact_person" [(ngModel)]="form.contact_person" [required]="true"></app-input>
            <app-input label="Phone number" name="phone_number" [(ngModel)]="form.phone_number" [required]="true"></app-input>
            <app-input label="Email" type="email" name="email" [(ngModel)]="form.email"></app-input>
            <app-input label="Location" name="location" [(ngModel)]="form.location"></app-input>
            <app-input label="Bond registration number" name="bond_registration_number" [(ngModel)]="form.bond_registration_number"></app-input>
            <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="submitting">
              @if (submitting) { <app-spinner></app-spinner> } @else { Register business profile }
            </app-button>
          </form>
        }
      </div>
    }
  `,
})
export class DealerProfileComponent implements OnInit {
  dealer: any = undefined;
  form = { ...EMPTY_FORM };
  error = '';
  submitting = false;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    try {
      const result = await this.api.call<any>('DealerService', 'myDealerProfile', {}, this.auth.token());
      this.dealer = result;
    } catch (err: any) {
      this.error = err.message;
    }
  }

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.submitting = true;
    try {
      await this.api.call('DealerService', 'registerOwnDealerProfile', this.form, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
