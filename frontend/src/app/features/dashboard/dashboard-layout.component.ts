import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dashboard-layout',
  imports: [MatIconModule],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardLayoutComponent {
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