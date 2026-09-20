import { Injectable, computed, signal } from '@angular/core';
import { ApiService } from './api.service';

export interface AuthUser {
  id: number;
  roleCode: string;
  firstName?: string;
  lastName?: string;
  [key: string]: unknown;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenSignal = signal<string | null>(localStorage.getItem('rau_token'));
  private userSignal = signal<AuthUser | null>(this.loadStoredUser());

  token = computed(() => this.tokenSignal());
  user = computed(() => this.userSignal());

  constructor(private api: ApiService) {}

  private loadStoredUser(): AuthUser | null {
    const stored = localStorage.getItem('rau_user');
    return stored ? JSON.parse(stored) : null;
  }

  async login(username: string, password: string): Promise<AuthUser> {
    const result = await this.api.call<{ token: string; user: AuthUser }>('Auth', 'login', { username, password });
    this.tokenSignal.set(result.token);
    this.userSignal.set(result.user);
    localStorage.setItem('rau_token', result.token);
    localStorage.setItem('rau_user', JSON.stringify(result.user));
    return result.user;
  }

  logout() {
    this.tokenSignal.set(null);
    this.userSignal.set(null);
    localStorage.removeItem('rau_token');
    localStorage.removeItem('rau_user');
  }

  register(fields: Record<string, unknown>) {
    return this.api.call('SystemUserModelService', 'registerUser', { User: JSON.stringify(fields) });
  }
}
