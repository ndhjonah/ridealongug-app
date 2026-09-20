import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { formatDate } from '../utils/format';
import { CardComponent } from '../shared/card.component';
import { InputComponent } from '../shared/input.component';
import { SelectComponent } from '../shared/select.component';
import { ButtonComponent } from '../shared/button.component';
import { StatusPillComponent } from '../shared/status-pill.component';
import { AlertComponent } from '../shared/alert.component';
import { SpinnerComponent } from '../shared/spinner.component';

const LICENCE_CLASSES = ['A', 'B', 'C', 'D', 'F', 'G'];

@Component({
  selector: 'app-my-licence',
  standalone: true,
  imports: [CommonModule, FormsModule, CardComponent, InputComponent, SelectComponent, ButtonComponent, StatusPillComponent, AlertComponent, SpinnerComponent],
  template: `
    @if (licence === undefined) {
      <div class="flex justify-center py-24">
        <app-spinner className="w-6 h-6 text-charcoal"></app-spinner>
      </div>
    } @else {
      <div class="max-w-lg mx-auto px-6 py-12">
        <h1 class="font-display text-2xl font-bold mb-8">My driving licence</h1>

        <app-alert className="mb-4" [message]="error"></app-alert>

        @if (licence) {
          <app-card className="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <p class="font-display font-semibold text-lg">{{ licence.licenceNumber }}</p>
              <app-status-pill [status]="licence.verificationStatus"></app-status-pill>
            </div>
            <dl class="text-sm space-y-1">
              <div class="flex justify-between"><dt class="text-charcoal">Class</dt><dd>{{ licence.licenceClass }}</dd></div>
              <div class="flex justify-between"><dt class="text-charcoal">Issued</dt><dd>{{ formatDate(licence.issueDate) }}</dd></div>
              <div class="flex justify-between"><dt class="text-charcoal">Expires</dt><dd>{{ formatDate(licence.expiryDate) }}</dd></div>
            </dl>
            @if (licence.verificationStatus === 'REJECTED' && licence.rejectionReason) {
              <app-alert [message]="licence.rejectionReason"></app-alert>
            }
            @if (licence.verificationStatus === 'PENDING') {
              <app-alert type="info" message="A Super Admin will verify your licence shortly."></app-alert>
            }
          </app-card>
        } @else {
          <form (submit)="handleSubmit($event)" class="space-y-4">
            <p class="text-charcoal text-sm mb-2">
              Submit your Ugandan driving licence to book vehicles without a platform driver.
            </p>
            <app-input label="Licence number" name="licence_number" [(ngModel)]="form.licence_number" [required]="true"></app-input>
            <app-select label="Licence class" name="licence_class" [(ngModel)]="form.licence_class">
              @for (c of licenceClasses; track c) {
                <option [value]="c">{{ c }}</option>
              }
            </app-select>
            <div class="grid grid-cols-2 gap-4">
              <app-input label="Issue date" type="date" name="issue_date" [(ngModel)]="form.issue_date" [required]="true"></app-input>
              <app-input label="Expiry date" type="date" name="expiry_date" [(ngModel)]="form.expiry_date" [required]="true"></app-input>
            </div>
            <label class="block">
              <span class="block text-sm font-medium text-charcoal mb-1">Licence photo</span>
              <input type="file" accept="image/*" (change)="onFileChange($event)" class="w-full text-sm" />
            </label>
            <app-button type="submit" variant="accent" [fullWidth]="true" [disabled]="submitting">
              @if (submitting) { <app-spinner></app-spinner> } @else { Submit for verification }
            </app-button>
          </form>
        }
      </div>
    }
  `,
})
export class MyLicenceComponent implements OnInit {
  licence: any = undefined;
  error = '';
  form = { licence_number: '', licence_class: 'B', issue_date: '', expiry_date: '' };
  file: File | null = null;
  submitting = false;
  licenceClasses = LICENCE_CLASSES;
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    try {
      const result = await this.api.call<any>('DrivingLicenceService', 'myLicence', {}, this.auth.token());
      this.licence = result;
    } catch (err: any) {
      this.error = err.message;
    }
  }

  onFileChange(e: Event) {
    const input = e.target as HTMLInputElement;
    this.file = input.files?.[0] || null;
  }

  private fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve((reader.result as string).split(',')[1]);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  async handleSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.submitting = true;
    try {
      const base64 = this.file ? await this.fileToBase64(this.file) : 'cGxhY2Vob2xkZXI=';
      await this.api.call(
        'DrivingLicenceService',
        'submitLicence',
        {
          licence_number: this.form.licence_number,
          licence_class: this.form.licence_class,
          issue_date: this.form.issue_date,
          expiry_date: this.form.expiry_date,
          licence_image_base64: base64,
        },
        this.auth.token()
      );
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.submitting = false;
    }
  }
}
