import { Component, ElementRef, HostListener, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="relative">
      <button
        (click)="open = !open"
        class="relative p-2 rounded-sign hover:bg-mist transition-colors"
        aria-label="Notifications"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 17h5l-1.4-1.4A2 2 0 0118 14.2V11a6 6 0 10-12 0v3.2a2 2 0 01-.6 1.4L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        @if (unreadCount() > 0) {
          <span class="absolute top-0.5 right-0.5 w-4 h-4 bg-brick text-white text-[10px] rounded-full flex items-center justify-center">
            {{ unreadCount() }}
          </span>
        }
      </button>
      @if (open) {
        <div class="absolute right-0 mt-2 w-80 bg-white border border-mist rounded-sign shadow-lg max-h-96 overflow-y-auto z-40">
          @if (notifications.length === 0) {
            <p class="p-4 text-sm text-charcoal">No notifications yet.</p>
          } @else {
            @for (n of notifications; track n.id) {
              <button
                (click)="markRead(n.id)"
                class="block w-full text-left p-3 border-b border-mist last:border-0 hover:bg-paper transition-colors"
                [ngClass]="{ 'bg-marigold/5': !n.isRead }"
              >
                <p class="text-sm font-semibold text-ink">{{ n.title }}</p>
                <p class="text-xs text-charcoal mt-0.5">{{ n.message }}</p>
              </button>
            }
          }
        </div>
      }
    </div>
  `,
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  open = false;
  notifications: any[] = [];
  private interval?: ReturnType<typeof setInterval>;

  constructor(private api: ApiService, private auth: AuthService, private host: ElementRef) {}

  ngOnInit() {
    this.load();
    this.interval = setInterval(() => this.load(), 30000);
  }

  ngOnDestroy() {
    if (this.interval) clearInterval(this.interval);
  }

  @HostListener('document:mousedown', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.host.nativeElement.contains(event.target)) this.open = false;
  }

  async load() {
    try {
      const result = await this.api.call('NotificationService', 'myNotifications', {}, this.auth.token());
      this.notifications = result || [];
    } catch {
      // notifications are non-critical
    }
  }

  unreadCount() {
    return this.notifications.filter((n) => !n.isRead).length;
  }

  async markRead(id: number) {
    try {
      await this.api.call('NotificationService', 'markAsRead', { notification_id: id }, this.auth.token());
      this.notifications = this.notifications.map((n) => (n.id === id ? { ...n, isRead: true } : n));
    } catch {
      // ignore
    }
  }
}
