import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { CardComponent } from '../shared/card.component';
import { InputComponent } from '../shared/input.component';
import { ButtonComponent } from '../shared/button.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';
import { StatusPillComponent } from '../shared/status-pill.component';
import { sanitizePhoneInput, validateEmail, validateName, validatePhone, runValidators, hasErrors } from '../utils/validation';

interface ProfileForm {
  first_name: string;
  last_name: string;
  email: string;
  phone_number: string;
  [key: string]: string;
}

const VALIDATORS: Record<keyof ProfileForm, (v: string) => string> = {
  first_name: (v) => validateName(v, 'First name'),
  last_name: (v) => validateName(v, 'Last name'),
  email: (v) => validateEmail(v),
  phone_number: (v) => validatePhone(v, { required: false }),
};

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, CardComponent, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent, StatusPillComponent],
  template: `
    @if (profile === undefined) {
      <div class="flex justify-center py-24">
        <app-spinner className="w-6 h-6 text-charcoal"></app-spinner>
      </div>
    } @else {
      <div class="max-w-lg mx-auto px-6 py-12">
        <div class="flex items-center justify-between mb-8">
          <h1 class="font-display text-2xl font-bold">My profile</h1>
          <app-status-pill [status]="profile.roleCode"></app-status-pill>
        </div>

        <app-card className="p-6">
          <form (submit)="handleSave($event)" class="space-y-4" novalidate>
            <div class="grid grid-cols-2 gap-4">
              <app-input label="First name" name="first_name" [(ngModel)]="form.first_name"
                (blurred)="validateField('first_name')" [error]="fieldErrors.first_name || ''" [required]="true"></app-input>
              <app-input label="Last name" name="last_name" [(ngModel)]="form.last_name"
                (blurred)="validateField('last_name')" [error]="fieldErrors.last_name || ''" [required]="true"></app-input>
            </div>
            <app-input label="Username" [ngModel]="profile.username" name="username" [disabled]="true" className="opacity-60 cursor-not-allowed"></app-input>
            <app-input label="Email" type="email" name="email" [(ngModel)]="form.email"
              (blurred)="validateField('email')" [error]="fieldErrors.email || ''" [required]="true"></app-input>
            <app-input label="Phone number" name="phone_number" [(ngModel)]="form.phone_number"
              (valueChange)="onPhoneChange($event)" (blurred)="validateField('phone_number')" [error]="fieldErrors.phone_number || ''"
              inputMode="numeric" [maxLength]="10" placeholder="e.g. 0772123456"></app-input>

            <app-alert [message]="error"></app-alert>
            <app-alert type="success" [message]="success"></app-alert>

            <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="saving">
              @if (saving) { <app-spinner></app-spinner> } @else { Save changes }
            </app-button>
          </form>
        </app-card>

        <app-card className="p-6 mt-6 border-brick/30">
          <h3 class="font-display font-semibold text-brick mb-2">Deactivate account</h3>
          <p class="text-sm text-charcoal mb-4">
            You'll be logged out immediately and won't be able to log back in. A Super Admin can
            permanently delete your account, or reactivate it, on request.
          </p>
          @if (!confirmingDeactivate) {
            <app-button variant="outline" (clicked)="confirmingDeactivate = true">Deactivate my account</app-button>
          } @else {
            <div class="flex gap-3">
              <app-button variant="ghost" (clicked)="confirmingDeactivate = false">Cancel</app-button>
              <app-button variant="danger" [disabled]="deactivating" (clicked)="handleDeactivate()">
                @if (deactivating) { <app-spinner></app-spinner> } @else { Yes, deactivate }
              </app-button>
            </div>
          }
        </app-card>
      </div>
    }
  `,
})
export class ProfileComponent implements OnInit {
  profile: any = undefined;
  form: ProfileForm = { first_name: '', last_name: '', email: '', phone_number: '' };
  fieldErrors: Partial<Record<keyof ProfileForm, string>> = {};
  error = '';
  success = '';
  saving = false;
  confirmingDeactivate = false;
  deactivating = false;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    try {
      const result = await this.api.call<any>('SystemUserModelService', 'myProfile', {}, this.auth.token());
      this.profile = result;
      this.form = {
        first_name: result.firstName || '',
        last_name: result.lastName || '',
        email: result.email || '',
        phone_number: result.phoneNumber || '',
      };
    } catch (err: any) {
      this.error = err.message;
    }
  }

  onPhoneChange(value: string) {
    this.form.phone_number = sanitizePhoneInput(value);
    if (this.fieldErrors.phone_number) this.validateField('phone_number');
  }

  validateField(field: keyof ProfileForm) {
    this.fieldErrors = { ...this.fieldErrors, [field]: VALIDATORS[field](this.form[field]) };
  }

  async handleSave(e: Event) {
    e.preventDefault();
    this.error = '';
    this.success = '';

    const errors = runValidators(this.form, VALIDATORS);
    this.fieldErrors = errors;
    if (hasErrors(errors)) {
      this.error = 'Please fix the highlighted fields before saving.';
      return;
    }

    this.saving = true;
    try {
      await this.api.call('SystemUserModelService', 'updateMyProfile', this.form, this.auth.token());
      this.success = 'Profile updated.';
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.saving = false;
    }
  }

  async handleDeactivate() {
    this.deactivating = true;
    this.error = '';
    try {
      await this.api.call('SystemUserModelService', 'deactivateMyAccount', {}, this.auth.token());
      this.auth.logout();
      this.router.navigateByUrl('/');
    } catch (err: any) {
      this.error = err.message;
      this.deactivating = false;
    }
  }
}
