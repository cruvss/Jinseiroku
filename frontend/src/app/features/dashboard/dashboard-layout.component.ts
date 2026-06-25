import { ChangeDetectionStrategy, Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MatIconModule } from '@angular/material/icon';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { VaultComponent } from '../vault/vault.component';
import { InboxService } from '../../core/services/inbox.service';
import { CryptoService } from '../../core/services/crypto.service';

@Component({
  selector: 'app-dashboard-layout',
  imports: [MatIconModule, RouterOutlet, VaultComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardLayoutComponent implements OnInit {
  protected router = inject(Router);
  private http = inject(HttpClient);
  private inboxService = inject(InboxService);
  private crypto = inject(CryptoService);
  
  activeTab = signal('dashboard');
  captureText = signal('');
  inboxCount = this.inboxService.unreadCount;
  isCapturing = signal(false);

  readonly navLinks = [
    { id: 'dashboard',     icon: 'show_chart',      label: 'Dashboard' },
    { id: 'inbox',         icon: 'inbox',            label: 'Quick Capture', hasBadge: true },
    { id: 'vault',         icon: 'lock',             label: 'Vault' },
    { id: 'subscriptions', icon: 'credit_card',      label: 'Subscriptions' },
    { id: 'tasks',         icon: 'event_available',  label: 'Tasks' },
    { id: 'timeline',      icon: 'history',          label: 'Timeline' },
  ];

  constructor() {
    const initialTab = this.router.url.split('/')[1] || 'dashboard';
    this.activeTab.set(initialTab);

    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe(() => {
      const currentTab = this.router.url.split('/')[1] || 'dashboard';
      this.activeTab.set(currentTab);
    });
  }

  ngOnInit() {
    this.http.get<{data: {unprocessedInboxCount: number, vaultDocumentCount: number}}>(`${environment.apiUrl}/dashboard`)
      .subscribe({
        next: (res) => {
          this.inboxCount.set(res.data.unprocessedInboxCount);
        },
        error: (err) => console.error('Failed to load dashboard stats', err)
      });
  }

  navigate(tabId: string) {
    this.router.navigate([`/${tabId}`]);
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