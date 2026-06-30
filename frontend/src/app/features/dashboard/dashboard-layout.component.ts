import { ChangeDetectionStrategy, Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MatIconModule } from '@angular/material/icon';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { VaultComponent } from '../vault/vault.component';
import { InboxService } from '../../core/services/inbox.service';
import { CryptoService } from '../../core/services/crypto.service';
import { Title } from '@angular/platform-browser';
import { SubscriptionService } from '../../core/services/subscription.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ProfileDialogComponent } from '../profile-dialog/profile-dialog.component';
@Component({
  selector: 'app-dashboard-layout',
  imports: [MatIconModule, RouterOutlet, CommonModule, MatDialogModule],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardLayoutComponent implements OnInit {
  protected router = inject(Router);
  private http = inject(HttpClient);
  private inboxService = inject(InboxService);
  private crypto = inject(CryptoService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  subscriptionService = inject(SubscriptionService);
  titleService = inject(Title);
  
  activeTab = signal('dashboard');
  captureText = signal('');
  inboxCount = this.inboxService.unreadCount;
  isCapturing = signal(false);
  userInitials = signal('U');

  readonly navLinks = [
    { id: 'dashboard',     icon: 'show_chart',      label: 'Dashboard' },
    { id: 'inbox',         icon: 'inbox',            label: 'Quick Capture', hasBadge: true },
    { id: 'vault',         icon: 'lock',             label: 'Vault' },
    { id: 'subscriptions', icon: 'credit_card',      label: 'Subscriptions' },
    { id: 'tasks',         icon: 'event_available',  label: 'Tasks' },
    { id: 'timeline',      icon: 'history',          label: 'Timeline' },
  ];

  constructor() {
    const path = window.location.pathname;
    const initialTab = path.split('/')[1] || 'dashboard';
    this.activeTab.set(initialTab);
    const formattedTitle = initialTab.charAt(0).toUpperCase() + initialTab.slice(1);
    this.titleService.setTitle(`${formattedTitle}`);

    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe(() => {
      const urlPath = this.router.url.split('?')[0].split('#')[0];
      const currentTab = urlPath.split('/')[1] || 'dashboard';
      this.activeTab.set(currentTab);
      
      const formattedTitle = currentTab.charAt(0).toUpperCase() + currentTab.slice(1);
      this.titleService.setTitle(`${formattedTitle}`);
    });
  }

    dashboardStats = signal<any>({
    unprocessedInboxCount: 0,
    vaultDocumentCount: 0,
    totalMonthlySubscriptionCost: 0,
    nextSubscriptionRenewalDate: null,
    overdueTasksCount: 0,
    pendingToDosCount: 0,
    nextRecurringTaskDueDate: null
  });

  ngOnInit() {
    this.subscriptionService.loadSubscriptions();
    
    this.authService.currentUser$.subscribe(user => {
      if (user && user.email) {
        const usernamePart = user.email.split('@')[0];
        const initials = usernamePart.substring(0, 2).toUpperCase();
        this.userInitials.set(initials);
      }
    });

    this.http.get<{ data: any }>(`${environment.apiUrl}/dashboard`)
      .subscribe({
        next: (res) => {
          this.dashboardStats.set(res.data);
          this.inboxCount.set(res.data.unprocessedInboxCount);
        },
        error: (err) => console.error('Failed to load dashboard stats', err)
      });
  }

  navigate(tabId: string) {
    this.router.navigate([`/${tabId}`]);
  }

  logout(){
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }

  openProfile() {
    this.dialog.open(ProfileDialogComponent,{
      width:'450px',
      panelClass: 'profile-dialog-panel'
    });
  }


  updateCapture(e: Event) {
    this.captureText.set((e.target as HTMLInputElement).value);
  }

  async capture(e: Event) {
    e.preventDefault();
    const text = this.captureText().trim();
    if (!text || this.isCapturing()) return;
    
    this.isCapturing.set(true);

    try {
      const dek = await this.crypto.generateDEK();
      const fd = new FormData();
      
      fd.append('encryptedDek', await this.crypto.wrapDEK(dek));

      const encText = await this.crypto.encryptText(text, dek);
      fd.append('textContentEncrypted', `${encText.iv}:${encText.ciphertext}`);

      this.inboxService.capture(fd).subscribe({
        next: () => {
          this.captureText.set('');
          this.inboxCount.update(n => n + 1);
          this.isCapturing.set(false);
        },
        error: (err) => {
          console.error('Failed to capture', err);
          this.isCapturing.set(false);
        }
      });
    } catch (e) {
      console.error(e);
      this.isCapturing.set(false);
    }
  }



}