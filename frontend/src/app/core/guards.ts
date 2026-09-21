import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.token()) return router.parseUrl('/login');
  return true;
};

export function roleGuard(roles: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.token()) return router.parseUrl('/login');
    const user = auth.user();
    if (!user || !roles.includes(user.roleCode)) return router.parseUrl('/');
    return true;
  };
}
