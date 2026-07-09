import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
  OnInit,
  computed,
} from '@angular/core';
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
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ProfileDialogComponent } from '../profile-dialog/profile-dialog.component';
import { TaskService } from '../../core/services/task.service';
import { TimelineService } from '../../core/services/timeline.service';
import { PaymentComponent } from '../payment/payment.component';

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
  taskService = inject(TaskService);
  timelineService = inject(TimelineService);
  titleService = inject(Title);

  activeTab = signal('dashboard');
  inboxCount = this.inboxService.unreadCount;
  isCapturing = signal(false);
  userInitials = signal('U');
  selectedFile: File | null = null;
  isDragOver = signal(false);

  // Search Signals
  searchQuery = signal('');
  showResults = signal(false);

  readonly navLinks = [
    { id: 'dashboard', icon: 'show_chart', label: 'Dashboard' },
    { id: 'inbox', icon: 'inbox', label: 'Quick Capture', hasBadge: true },
    { id: 'vault', icon: 'lock', label: 'Vault' },
    { id: 'subscriptions', icon: 'credit_card', label: 'Subscriptions' },
    { id: 'tasks', icon: 'event_available', label: 'Tasks' },
    { id: 'timeline', icon: 'history', label: 'Timeline' },
  ];

  // Dynamic search matching across Tasks, Timeline events, and Subscriptions
  searchResults = computed(() => {
    const query = this.searchQuery().trim().toLowerCase();
    if (!query) return [];

    const results: { type: string; title: string; subtitle: string; icon: string; tab: string }[] =
      [];

    // 1. Search Tasks
    this.taskService.tasks().forEach((task) => {
      const title = task.decryptedTitle || '';
      const desc = task.decryptedDescription || '';
      if (title.toLowerCase().includes(query) || desc.toLowerCase().includes(query)) {
        results.push({
          type: 'Task',
          title: title,
          subtitle: `Category: ${task.category} · Status: ${task.status}`,
          icon: 'event_available',
          tab: 'tasks',
        });
      }
    });

    // 2. Search Timeline Events
    this.timelineService.events().forEach((event) => {
      const title = event.decryptedTitle || '';
      const desc = event.decryptedDescription || '';
      if (title.toLowerCase().includes(query) || desc.toLowerCase().includes(query)) {
        results.push({
          type: 'Timeline',
          title: title,
          subtitle: `Category: ${event.category} · Date: ${event.eventDate}`,
          icon: 'history',
          tab: 'timeline',
        });
      }
    });

    // 3. Search Subscriptions
    this.subscriptionService.subscriptions().forEach((sub) => {
      const name = sub.name || '';
      if (name.toLowerCase().includes(query)) {
        results.push({
          type: 'Subscription',
          title: name,
          subtitle: `Cost: ${sub.cost} ${sub.currency} · Cycle: ${sub.billingCycle}`,
          icon: 'credit_card',
          tab: 'subscriptions',
        });
      }
    });

    return results.slice(0, 8); // Top 8 matches
  });

  constructor() {
    
    const path = window.location.pathname;
    const initialTab = path.split('/')[1] || 'dashboard';
    this.activeTab.set(initialTab);
    const formattedTitle = initialTab.charAt(0).toUpperCase() + initialTab.slice(1);
    this.titleService.setTitle(`${formattedTitle}`);

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(() => {
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
    nextRecurringTaskDueDate: null,
  });

  ngOnInit() {
    this.subscriptionService.loadSubscriptions();

    // Load tasks and events so the client-side cache is ready for search query matches
    this.taskService.loadTasks().subscribe({ error: () => {} });
    this.timelineService.loadEvents().subscribe({ error: () => {} });

    this.authService.currentUser$.subscribe((user) => {
      if (user && user.email) {
        const usernamePart = user.email.split('@')[0];
        const initials = usernamePart.substring(0, 2).toUpperCase();
        this.userInitials.set(initials);
      }
    });

    this.http.get<{ data: any }>(`${environment.apiUrl}/dashboard`).subscribe({
      next: (res) => {
        this.dashboardStats.set(res.data);
        this.inboxCount.set(res.data.unprocessedInboxCount);
      },
      error: (err) => console.error('Failed to load dashboard stats', err),
    });
  }

  navigate(tabId: string) {
    this.router.navigate([`/${tabId}`]);
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }

  openProfile() {
    this.dialog.open(ProfileDialogComponent, {
      width: '450px',
      panelClass: 'profile-dialog-panel',
    });
  }

  onSearchInput(e: Event) {
    this.searchQuery.set((e.target as HTMLInputElement).value);
  }

  onFileSelected(e: any) {
    this.selectedFile = e.target.files[0];
  }


  onSearchFocus() {
    this.showResults.set(true);
  }

  onSearchBlur() {
    // 200ms delay allows the mousedown events to register navigation triggers
    setTimeout(() => this.showResults.set(false), 200);
  }

  goToResult(result: any) {
    this.searchQuery.set('');
    this.router.navigate([`/${result.tab}`]);
  }

  openDialog(){
    const dialogRef = this.dialog.open(PaymentComponent,{
      width:'1500px',
      panelClass: 'rounded-dialog'
    });

  }



  async capture(e: Event, inputEl: HTMLInputElement) {
    e.preventDefault();
    const text = inputEl.value.trim();
    if (!text && !this.selectedFile) return;

    this.isCapturing.set(true);

    try {
      const dek = await this.crypto.generateDEK();
      const fd = new FormData();

      fd.append('encryptedDek', await this.crypto.wrapDEK(dek));

      let contentText = text;
      if (!contentText && this.selectedFile) {
        contentText = this.selectedFile.name;
      }

      if (contentText) {
        const encText = await this.crypto.encryptText(contentText, dek);
        fd.append('textContentEncrypted', `${encText.iv}:${encText.ciphertext}`);
      }

      if (this.selectedFile) {
        const fileBytes = await this.selectedFile.arrayBuffer();
        const encFile = await this.crypto.encryptFile(fileBytes, dek);
        const encryptedBlob = new Blob([encFile.iv as BufferSource, encFile.ciphertext], {
          type: this.selectedFile.type || 'application/octet-stream',
        });
        fd.append('file', encryptedBlob, 'file.bin');
      }

      this.inboxService.capture(fd).subscribe({
        next: () => {
          inputEl.value = '';
          this.selectedFile = null;
          this.inboxCount.update((n) => n + 1);
          this.dashboardStats.update((stats) => ({
            ...stats,
            unprocessedInboxCount: stats.unprocessedInboxCount + 1,
          }));
          this.isCapturing.set(false);
        },
        error: (err) => {
          console.error('Failed to capture', err);
          this.isCapturing.set(false);
        },
      });
    } catch (e) {
      console.error(e);
      this.isCapturing.set(false);
    }
  }
}
