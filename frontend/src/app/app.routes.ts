import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RecoveryKeyDialogComponent } from './features/auth/register/recovery-key-dialog.component';

export const routes: Routes = [
    {path:'login', component: LoginComponent},
    {path:'register',component: RecoveryKeyDialogComponent}
];
