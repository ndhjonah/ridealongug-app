import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { InputComponent } from '../shared/input.component';
import { ButtonComponent } from '../shared/button.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent],
  template: `
    <div class="max-w-sm mx-auto px-6 py-20">
      <h1 class="font-display text-2xl font-bold mb-1">Enter your reset code</h1>
      <p class="text-charcoal mb-8">Check your email for an 8-character code.</p>

      <form (submit)="handleSubmit($event)" class="space-y-4">
        <app-input label="Reset code" name="token" [(ngModel)]="token" (valueChange)="token = $event.toUpperCase()" [required]="true"></app-input>
        <app-input label="New password" type="password" name="newPassword" [(ngModel)]="newPassword" [required]="true"></app-input>
        <app-alert [message]="error"></app-alert>
        <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="loading">
          @if (loading) { <app-spinner></app-spinner> } @else { Reset password }
        </app-button>
      </form>
    </div>
  `,
})
export class ResetPasswordComponent {
  token = '';
  newPassword = '';
  error = '';
  loading = false;

  constructor(private api: ApiService, private router: Router) {}

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.loading = true;
    try {
      await this.api.call('Auth', 'resetPassword', { token: this.token, new_password: this.newPassword });
      this.router.navigateByUrl('/login');
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
