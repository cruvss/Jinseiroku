import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { VaultComponent } from '../vault/vault.component';

@Component({
  selector: 'app-dashboard-layout',
  imports: [MatIconModule, RouterOutlet, VaultComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardLayoutComponent {
  protected router = inject(Router);
  activeTab = signal('dashboard');
  captureText = signal('');
  inboxCount = signal(3);

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

  navigate(tabId: string) {
    this.router.navigate([`/${tabId}`]);
  }

  updateCapture(e: Event) {
    this.captureText.set((e.target as HTMLInputElement).value);
  }

  capture(e: Event) {
    e.preventDefault();
    const text = this.captureText().trim();
    if (!text) return;
    this.inboxCount.update(n => n + 1);
    this.captureText.set('');
  }
}