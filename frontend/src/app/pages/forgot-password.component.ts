import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { InputComponent } from '../shared/input.component';
import { ButtonComponent } from '../shared/button.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, RouterLink, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent],
  template: `
    <div class="max-w-sm mx-auto px-6 py-20">
      <h1 class="font-display text-2xl font-bold mb-1">Reset your password</h1>
      <p class="text-charcoal mb-8">We'll email you a reset code.</p>

      @if (message) {
        <app-alert type="success" [message]="message"></app-alert>
        <app-button variant="accent" [fullWidth]="true" className="mt-4" (clicked)="router.navigateByUrl('/reset-password')">
          I have my code
        </app-button>
      } @else {
        <form (submit)="handleSubmit($event)" class="space-y-4">
          <app-input label="Email" type="email" name="email" [(ngModel)]="email" [required]="true"></app-input>
          <app-alert [message]="error"></app-alert>
          <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="loading">
            @if (loading) { <app-spinner></app-spinner> } @else { Send reset code }
          </app-button>
        </form>
      }

      <p class="mt-6 text-sm text-charcoal text-center">
        <a routerLink="/login" class="underline text-ink">Back to login</a>
      </p>
    </div>
  `,
})
export class ForgotPasswordComponent {
  email = '';
  message = '';
  error = '';
  loading = false;

  constructor(private api: ApiService, public router: Router) {}

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.loading = true;
    try {
      await this.api.call('Auth', 'forgotPassword', { email: this.email });
      this.message = 'If an account with that email exists, a reset code has been sent.';
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
