import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout.component';

export const routes: Routes = [
    // Public routes (no authentication needed)
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    
    // Protected routes (authentication required)
    {
        path: 'dashboard',
        component: DashboardLayoutComponent,
        canActivate: [authGuard]
    },
    
    // Default route - redirect to dashboard
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    
    // Wildcard route - redirect to dashboard if route not found
    { path: '**', redirectTo: '/dashboard' }
];