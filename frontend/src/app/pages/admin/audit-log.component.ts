import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { formatDate } from '../../utils/format';
import { CardComponent } from '../../shared/card.component';
import { AlertComponent } from '../../shared/alert.component';
import { SpinnerComponent } from '../../shared/spinner.component';
import { EmptyStateComponent } from '../../shared/empty-state.component';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, CardComponent, AlertComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading) {
      <div class="flex justify-center py-16"><app-spinner className="w-6 h-6 text-charcoal"></app-spinner></div>
    } @else {
      <div>
        <app-alert className="mb-4" [message]="error"></app-alert>
        @if (logs.length === 0) {
          <app-empty-state title="No actions logged yet"></app-empty-state>
        } @else {
          <div class="space-y-2">
            @for (l of logs; track l.id) {
              <app-card className="p-4 flex items-center justify-between text-sm">
                <div>
                  <span class="font-medium">{{ l.action }}</span>
                  <span class="text-charcoal"> · {{ l.targetEntity }} #{{ l.targetId }}</span>
                  @if (l.details) { <p class="text-charcoal text-xs mt-1">{{ l.details }}</p> }
                </div>
                <span class="text-charcoal text-xs">{{ formatDate(l.performedAt) }}</span>
              </app-card>
            }
          </div>
        }
      </div>
    }
  `,
})
export class AuditLogComponent implements OnInit {
  logs: any[] = [];
  loading = true;
  error = '';
  formatDate = formatDate;

  constructor(private api: ApiService, private auth: AuthService) {}

  async ngOnInit() {
    try {
      const result = await this.api.call<any[]>('AuditLogService', 'viewLogs', {}, this.auth.token());
      this.logs = result || [];
    } catch (err: any) {
      this.error = err.message;
    } finally {
      this.loading = false;
    }
  }
}
