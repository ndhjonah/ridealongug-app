import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CardComponent } from '../../shared/card.component';
import { SelectComponent } from '../../shared/select.component';
import { ButtonComponent } from '../../shared/button.component';
import { StatusPillComponent } from '../../shared/status-pill.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

const ROLES = ['CUSTOMER', 'DRIVER', 'VEHICLE_OWNER', 'ADMINISTRATOR'];

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, CardComponent, SelectComponent, ButtonComponent, StatusPillComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    <div>
      <div class="flex justify-end mb-4">
        <app-select name="roleFilter" [(ngModel)]="roleFilter" (valueChange)="load()" className="w-auto">
          <option value="">All roles</option>
          @for (r of roles; track r) { <option [value]="r">{{ r.replaceAll('_', ' ') }}</option> }
        </app-select>
      </div>

      @if (loading) {
        <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
      }

      <app-alert className="mb-4" [message]="error"></app-alert>

      @if (!loading && users.length === 0) {
        <app-empty-state title="No users found"></app-empty-state>
      }

      @if (!loading && users.length > 0) {
        <div class="space-y-3">
          @for (u of users; track u.id) {
            <app-card className="p-5 flex items-center justify-between gap-4">
              <div>
                <p class="font-medium">{{ u.firstName }} {{ u.lastName }} <span class="text-charcoal font-normal">· {{ u.username }}</span></p>
                <p class="text-sm text-charcoal">{{ u.email }} {{ u.phoneNumber ? '· ' + u.phoneNumber : '' }}</p>
                @if (u.roleCode === 'DRIVER' && driverProfileFor(u.id); as dp) {
                  <p class="text-xs text-charcoal mt-1">
                    Licence {{ dp.licenceNumber }} ({{ dp.licenceClass }}) · {{ dp.yearsOfExperience ?? 0 }} yrs ·
                    {{ dp.isAvailable ? 'Available' : 'Unavailable' }}
                  </p>
                }
              </div>
              <div class="flex items-center gap-3">
                <app-status-pill [status]="u.roleCode"></app-status-pill>
                <app-status-pill [status]="u.isActive ? 'APPROVED' : 'REJECTED'"></app-status-pill>
                @if (u.id !== currentUser()?.id) {
                  <app-button variant="danger" [disabled]="busyId === u.id" (clicked)="handleDelete(u.id)">
                    @if (busyId === u.id) { <app-spinner></app-spinner> } @else { Delete }
                  </app-button>
                }
              </div>
            </app-card>
          }
        </div>
      }
    </div>
  `,
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  driverProfiles: any[] = [];
  roleFilter = '';
  loading = true;
  error = '';
  busyId: number | null = null;
  roles = ROLES;

  currentUser = this.auth.user;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    this.load();
  }

  async load() {
    this.loading = true;
    try {
      const result = await this.api.call<any[]>(
        'SystemUserModelService',
        'listAllUsers',
        this.roleFilter ? { role_code: this.roleFilter } : {},
        this.auth.token()
      );
      this.users = result || [];
      if (this.roleFilter === 'DRIVER' || !this.roleFilter) {
        const profiles = await this.api.call<any[]>('DriverProfileService', 'listAll', {}, this.auth.token());
        this.driverProfiles = profiles || [];
      }
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }

  driverProfileFor(userId: number) {
    return this.driverProfiles.find((p) => p.userId === userId);
  }

  async handleDelete(id: number) {
    if (!window.confirm('Permanently delete this user? This cannot be undone.')) return;
    this.busyId = id;
    try {
      await this.api.call('SystemUserModelService', 'deleteUser', { id }, this.auth.token());
      await this.load();
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.busyId = null;
    }
  }
}
