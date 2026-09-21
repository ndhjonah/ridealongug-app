import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/landing.component').then((m) => m.LandingComponent) },
  { path: 'login', loadComponent: () => import('./pages/login.component').then((m) => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./pages/register.component').then((m) => m.RegisterComponent) },
  { path: 'forgot-password', loadComponent: () => import('./pages/forgot-password.component').then((m) => m.ForgotPasswordComponent) },
  { path: 'reset-password', loadComponent: () => import('./pages/reset-password.component').then((m) => m.ResetPasswordComponent) },
  { path: 'vehicles/:id', loadComponent: () => import('./pages/vehicle-detail.component').then((m) => m.VehicleDetailComponent) },

  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/profile.component').then((m) => m.ProfileComponent),
  },
  {
    path: 'my-bookings',
    canActivate: [roleGuard(['CUSTOMER'])],
    loadComponent: () => import('./pages/my-bookings.component').then((m) => m.MyBookingsComponent),
  },
  {
    path: 'my-licence',
    canActivate: [roleGuard(['CUSTOMER'])],
    loadComponent: () => import('./pages/my-licence.component').then((m) => m.MyLicenceComponent),
  },
  {
    path: 'driver',
    canActivate: [roleGuard(['DRIVER'])],
    loadComponent: () => import('./pages/driver-dashboard.component').then((m) => m.DriverDashboardComponent),
  },

  {
    path: 'owner/vehicles',
    canActivate: [roleGuard(['VEHICLE_OWNER'])],
    loadComponent: () => import('./pages/owner/my-vehicles.component').then((m) => m.MyVehiclesComponent),
  },
  {
    path: 'owner/pickups',
    canActivate: [roleGuard(['VEHICLE_OWNER'])],
    loadComponent: () => import('./pages/owner/pending-pickups.component').then((m) => m.PendingPickupsComponent),
  },
  {
    path: 'owner/earnings',
    canActivate: [roleGuard(['VEHICLE_OWNER'])],
    loadComponent: () => import('./pages/owner/my-earnings.component').then((m) => m.MyEarningsComponent),
  },
  {
    path: 'owner/dealer',
    canActivate: [roleGuard(['VEHICLE_OWNER'])],
    loadComponent: () => import('./pages/owner/dealer-profile.component').then((m) => m.DealerProfileComponent),
  },

  {
    path: 'admin',
    canActivate: [roleGuard(['ADMINISTRATOR'])],
    loadComponent: () => import('./pages/admin/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
  },

  { path: '**', loadComponent: () => import('./pages/not-found.component').then((m) => m.NotFoundComponent) },
];
