import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { InputComponent } from '../shared/input.component';
import { SelectComponent } from '../shared/select.component';
import { ButtonComponent } from '../shared/button.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';
import {
  sanitizePhoneInput,
  validateEmail,
  validateName,
  validatePassword,
  validatePhone,
  validateUsername,
  runValidators,
  hasErrors,
} from '../utils/validation';

const ROLES = [
  { value: 'CUSTOMER', label: 'Customer - I want to rent vehicles' },
  { value: 'DRIVER', label: 'Driver - I want to drive for the platform' },
  { value: 'VEHICLE_OWNER', label: 'Vehicle owner - I want to list my own vehicles' },
];

interface RegisterForm {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phoneNumber: string;
  password: string;
  roleCode: string;
  [key: string]: string;
}

const VALIDATORS: Partial<Record<keyof RegisterForm, (v: string) => string>> = {
  firstName: (v) => validateName(v, 'First name'),
  lastName: (v) => validateName(v, 'Last name'),
  username: (v) => validateUsername(v),
  email: (v) => validateEmail(v),
  phoneNumber: (v) => validatePhone(v, { required: false }),
  password: (v) => validatePassword(v),
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, InputComponent, SelectComponent, ButtonComponent, AlertComponent, SpinnerComponent],
  template: `
    @if (success) {
      <div class="max-w-sm mx-auto px-6 py-24 text-center">
        <h1 class="font-display text-2xl font-bold mb-3">Account created</h1>
        <p class="text-charcoal mb-8">You can now log in with your new account.</p>
        <app-button variant="accent" (clicked)="router.navigateByUrl('/login')">Go to login</app-button>
      </div>
    } @else {
      <div class="max-w-md mx-auto px-6 py-16">
        <h1 class="font-display text-2xl font-bold mb-1">Create your account</h1>
        <p class="text-charcoal mb-8">Join RideAlongUG as a renter, driver, or vehicle owner.</p>

        <form (submit)="handleSubmit($event)" class="space-y-4" novalidate>
          <div class="grid grid-cols-2 gap-4">
            <app-input label="First name" name="firstName" [(ngModel)]="form.firstName"
              (blurred)="validateField('firstName')" [error]="fieldErrors.firstName || ''" [required]="true"></app-input>
            <app-input label="Last name" name="lastName" [(ngModel)]="form.lastName"
              (blurred)="validateField('lastName')" [error]="fieldErrors.lastName || ''" [required]="true"></app-input>
          </div>
          <app-input label="Username" name="username" [(ngModel)]="form.username"
            (blurred)="validateField('username')" [error]="fieldErrors.username || ''" [required]="true"></app-input>
          <app-input label="Email" type="email" name="email" [(ngModel)]="form.email"
            (blurred)="validateField('email')" [error]="fieldErrors.email || ''" [required]="true"></app-input>
          <app-input label="Phone number" name="phoneNumber" [(ngModel)]="form.phoneNumber"
            (valueChange)="onPhoneChange($event)" (blurred)="validateField('phoneNumber')" [error]="fieldErrors.phoneNumber || ''"
            inputMode="numeric" [maxLength]="10" placeholder="e.g. 0772123456"></app-input>
          <app-input label="Password" type="password" name="password" [(ngModel)]="form.password"
            (blurred)="validateField('password')" [error]="fieldErrors.password || ''" [required]="true"></app-input>
          <app-select label="I am a..." name="roleCode" [(ngModel)]="form.roleCode">
            @for (r of roles; track r.value) {
              <option [value]="r.value">{{ r.label }}</option>
            }
          </app-select>

          <app-alert [message]="error"></app-alert>

          <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="loading">
            @if (loading) {
              <app-spinner></app-spinner>
            } @else {
              Create account
            }
          </app-button>
        </form>

        <p class="mt-6 text-sm text-charcoal text-center">
          Already have an account? <a routerLink="/login" class="underline text-ink">Log in</a>
        </p>
      </div>
    }
  `,
})
export class RegisterComponent {
  roles = ROLES;
  form: RegisterForm = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    phoneNumber: '',
    password: '',
    roleCode: 'CUSTOMER',
  };
  fieldErrors: Partial<Record<keyof RegisterForm, string>> = {};
  error = '';
  success = false;
  loading = false;

  constructor(private auth: AuthService, public router: Router) {}

  onPhoneChange(value: string) {
    this.form.phoneNumber = sanitizePhoneInput(value);
    if (this.fieldErrors.phoneNumber) this.validateField('phoneNumber');
  }

  validateField(field: keyof RegisterForm) {
    const validator = VALIDATORS[field];
    if (!validator) return;
    this.fieldErrors = { ...this.fieldErrors, [field]: validator(this.form[field]) };
  }

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';

    const errors = runValidators(this.form, VALIDATORS);
    this.fieldErrors = errors;
    if (hasErrors(errors)) {
      this.error = 'Please fix the highlighted fields before continuing.';
      return;
    }

    this.loading = true;
    try {
      await this.auth.register(this.form);
      this.success = true;
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
