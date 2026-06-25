import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardLayoutComponent } from './features/dashboard/dashboard-layout.component';
import { VaultComponent } from './features/vault/vault.component';
import { SubscriptionsComponent } from './features/subscription/subscription.component';
import { InboxComponent } from './features/inbox/inbox.component';

export const routes: Routes = [
    // Public routes (no authentication needed)
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    
    // Protected routes wrapped in shell layout
    {
        path: '',
        component: DashboardLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: 'dashboard', children: [] },
            { path: 'vault', component: VaultComponent },
            { path: 'subscriptions', component: SubscriptionsComponent},
            { path: 'inbox', component: InboxComponent}
        ]
    },
    
    // Default route - redirect to dashboard
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    
    // Wildcard route - redirect to dashboard if route not found
    { path: '**', redirectTo: '/dashboard' }
];