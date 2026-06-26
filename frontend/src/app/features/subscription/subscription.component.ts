import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { SubscriptionService } from '../../core/services/subscription.service';
import { Subscription } from '../../core/models/subscription.model';
import { SubscriptionDialogComponent } from '../subscription-dialog/subscription-dialog.component';

@Component({
  selector: 'app-subscription',
  imports: [
    CommonModule, 
    MatCardModule, 
    MatButtonModule, 
    MatIconModule, 
    MatDialogModule, 
    MatChipsModule
  ],
  templateUrl: './subscription.component.html',
  styleUrl: './subscription.component.scss',
})
export class SubscriptionComponent implements OnInit {
  protected subscriptionService = inject(SubscriptionService);
  private dialog = inject(MatDialog);

  // subscriptions = signal<Subscription[]>([]);
  subscriptions = this.subscriptionService.subscriptions;

  currencySymbols: Record<string, string> = {
    USD: '$',
    EUR: '€',
    GBP: '£',
    JPY: '¥',
    NPR: 'रु'
  };
  
  // // Calculate total yearly cost dynamically
  // totalYearly = computed(() => {
  //   return this.subscriptions().reduce((total, sub) => {
  //     const cost = sub.cost;
  //     if (sub.status !== 'ACTIVE') return total;

  //     switch (sub.billingCycle) {
  //       case 'YEARLY': return total + cost;
  //       case 'MONTHLY': return total + (cost * 12);
  //       case 'WEEKLY': return total + (cost * 52);
  //       case 'FORTNIGHTLY': return total + (cost * 26);
  //       case 'QUARTERLY': return total + (cost * 4);
  //       case 'SEMI-YEARLY': return total + (cost * 2);
  //       default: return total;
  //     }
  //   }, 0);
  // });

  ngOnInit() {
    this.subscriptionService.loadSubscriptions();
  }

  loadSubscriptions() {
    this.subscriptionService.getSubscriptions().subscribe({
      next: (data) => this.subscriptions.set(data),
      error: (err) => console.error('Failed to load subscriptions', err)
    });
  }

  openDialog(subscription?: Subscription, inboxText?: string) {
    const dialogRef = this.dialog.open(SubscriptionDialogComponent, {
      width: '600px',
      panelClass: 'rounded-dialog',
      data: { subscription, inboxText }
    });

    dialogRef.afterClosed().subscribe((result: Subscription) => {
      if (result) {
        if (result.id) {
          this.subscriptionService.updateSubscription(result.id, result).subscribe(() => {
            this.loadSubscriptions();
          });
        } else {
          this.subscriptionService.createSubscription(result).subscribe(() => {
            this.loadSubscriptions();
          });
        }
      }
    });
  }

  deleteSubscription(id: string) {
    if (confirm('Are you sure you want to delete this subscription?')) {
      this.subscriptionService.deleteSubscription(id).subscribe(() => {
        this.loadSubscriptions();
      });
    }
  }
}
