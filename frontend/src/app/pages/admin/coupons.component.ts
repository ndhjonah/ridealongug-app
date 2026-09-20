import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { InputComponent } from '../../shared/input.component';
import { ButtonComponent } from '../../shared/button.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { CardComponent } from '../../shared/card.component';

const EMPTY_FORM = { code: '', discount_percentage: '', valid_from: '', valid_to: '', max_uses: '' };

@Component({
  selector: 'app-coupons',
  standalone: true,
  imports: [FormsModule, InputComponent, ButtonComponent, AlertComponent, SpinnerComponent, CardComponent],
  template: `
    <app-card className="p-6 max-w-lg">
      <h3 class="font-display font-semibold mb-4">Create a discount coupon</h3>
      <form (submit)="handleSubmit($event)" class="space-y-4">
        <app-input label="Code" name="code" [(ngModel)]="form.code" (valueChange)="form.code = $event.toUpperCase()" [required]="true"></app-input>
        <app-input label="Discount percentage" type="number" name="discount_percentage" [(ngModel)]="form.discount_percentage" [required]="true"></app-input>
        <div class="grid grid-cols-2 gap-4">
          <app-input label="Valid from" type="date" name="valid_from" [(ngModel)]="form.valid_from" [required]="true"></app-input>
          <app-input label="Valid to" type="date" name="valid_to" [(ngModel)]="form.valid_to" [required]="true"></app-input>
        </div>
        <app-input label="Max uses (optional)" type="number" name="max_uses" [(ngModel)]="form.max_uses"></app-input>
        <app-alert [message]="error"></app-alert>
        <app-alert type="success" [message]="success"></app-alert>
        <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="submitting">
          @if (submitting) { <app-spinner></app-spinner> } @else { Create coupon }
        </app-button>
      </form>
    </app-card>
  `,
})
export class CouponsComponent {
  form = { ...EMPTY_FORM };
  error = '';
  success = '';
  submitting = false;

  constructor(private api: ApiService, private auth: AuthService) {}

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.success = '';
    this.submitting = true;
    try {
      await this.api.call(
        'DiscountCouponService',
        'createCoupon',
        {
          code: this.form.code,
          discount_percentage: Number(this.form.discount_percentage),
          valid_from: this.form.valid_from,
          valid_to: this.form.valid_to,
          max_uses: this.form.max_uses ? Number(this.form.max_uses) : undefined,
        },
        this.auth.token()
      );
      this.success = `Coupon ${this.form.code.toUpperCase()} created.`;
      this.form = { ...EMPTY_FORM };
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
