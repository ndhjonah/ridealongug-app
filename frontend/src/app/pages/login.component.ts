import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { InputComponent } from '../shared/input.component';
import { ButtonComponent } from '../shared/button.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent],
  template: `
    <div class="max-w-sm mx-auto px-6 py-20">
      <h1 class="font-display text-2xl font-bold mb-1">Welcome back</h1>
      <p class="text-charcoal mb-8">Log in to book, list, or manage your vehicles.</p>

      <form (submit)="handleSubmit($event)" class="space-y-4">
        <app-input label="Username or email" [(ngModel)]="username" name="username" [required]="true"></app-input>
        <app-input label="Password" type="password" [(ngModel)]="password" name="password" [required]="true"></app-input>

        <app-alert [message]="error"></app-alert>

        <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="loading">
          @if (loading) {
            <app-spinner></app-spinner>
          } @else {
            Log in
          }
        </app-button>
      </form>

      <div class="mt-6 flex justify-between text-sm">
        <a routerLink="/forgot-password" class="text-charcoal hover:text-ink underline">Forgot password?</a>
        <a routerLink="/register" class="text-charcoal hover:text-ink underline">Create an account</a>
      </div>
    </div>
  `,
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.loading = true;
    try {
      await this.auth.login(this.username, this.password);
      this.router.navigateByUrl('/');
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
