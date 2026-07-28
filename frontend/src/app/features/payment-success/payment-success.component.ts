import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; font-family: sans-serif;">
      @if (loading) {
        <mat-spinner></mat-spinner>
        <p style="margin-top: 20px;">Verifying payment and upgrading your vault...</p>
      } @else if (error) {
        <h2 style="color: #f44336;">Upgrade Verification Failed</h2>
        <p>{{ errorMessage }}</p>
        <button (click)="goHome()" style="padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">Go to Dashboard</button>
      } @else {
        <h2 style="color: #4caf50;">Vault Upgraded Successfully!</h2>
        <p>Redirecting you to dashboard...</p>
      }
    </div>
  `
})
export class PaymentSuccessComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);

  loading = true;
  error = false;
  errorMessage = '';

  ngOnInit() {
    const sessionId = this.route.snapshot.queryParamMap.get('session_id');
    if (sessionId) {
      this.http.get(`${environment.apiUrl}/payment/verify?session_id=${sessionId}`).subscribe({
        next: () => {
          this.loading = false;
          setTimeout(() => {
            this.router.navigate(['/']);
          }, 3000);
        },
        error: (err) => {
          this.loading = false;
          this.error = true;
          this.errorMessage = err.error?.message || 'Verification failed. Please contact support.';
        }
      });
    } else {
      this.loading = false;
      this.error = true;
      this.errorMessage = 'Invalid checkout session.';
    }
  }

  goHome() {
    this.router.navigate(['/']);
  }
}