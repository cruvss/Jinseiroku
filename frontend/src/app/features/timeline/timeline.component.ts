import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TimelineService } from '../../core/services/timeline.service';
import { TimelineEvent } from '../../core/models/timeline.model';
import { TimelineDialogComponent } from '../timeline-dialog/timeline-dialog.component';
import { CryptoService } from '../../core/services/crypto.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

interface GroupedEvents {
  year: string;
  months: {
    month: string;
    events: TimelineEvent[];
  }[];
}

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: 'timeline.component.html',
  styleUrl: 'timeline.component.scss'
})
export class TimelineComponent implements OnInit {
  timelineService = inject(TimelineService);
  cryptoService = inject(CryptoService);
  dialog = inject(MatDialog);
  snackBar = inject(MatSnackBar);
  http = inject(HttpClient);
  fb = inject(FormBuilder);

  isLoading = signal(false);

  unlockForm: FormGroup = this.fb.group({
    password: ['', [Validators.required]]
  });

  ngOnInit() {
    if (this.cryptoService.hasActiveSession()) {
      this.load();
    }
  }

  load() {
    this.isLoading.set(true);
    this.timelineService.loadEvents().subscribe({
      next: () => this.isLoading.set(false),
      error: () => this.isLoading.set(false)
    });
  }

  async onUnlock() {
    if (this.unlockForm.invalid) return;
    this.isLoading.set(true);
    try {
      const email = localStorage.getItem('user_email') || '';
      this.http.get<any>(`${environment.apiUrl}/auth/salt?email=${email}`).subscribe({
        next: async (res) => {
          const params = res.data;
          const password = this.unlockForm.value.password;
          await this.cryptoService.initializeSession(password, params.encryptionSalt);
          if (params.encryptedKekVerification) {
            const isCorrect = await this.cryptoService.verifyKek(params.encryptedKekVerification);
            if (!isCorrect) {
              this.cryptoService.clearSession();
              this.isLoading.set(false);
              this.snackBar.open('Incorrect master password.', 'Close', { duration: 5000 });
              return;
            }
          }
          this.load();
        },
        error: () => {
          this.isLoading.set(false);
          this.snackBar.open('Failed to retrieve security credentials.', 'Close', { duration: 5000 });
        }
      });
    } catch (e) {
      this.snackBar.open('Error deriving encryption keys.', 'Close', { duration: 5000 });
      this.isLoading.set(false);
    }
  }

  groupedEvents = computed<GroupedEvents[]>(() => {
    const rawEvents = this.timelineService.events();
    const sorted = [...rawEvents].sort((a, b) => new Date(b.eventDate).getTime() - new Date(a.eventDate).getTime());
    const groups: { [year: string]: { [month: string]: TimelineEvent[] } } = {};

    for (const e of sorted) {
      const date = new Date(e.eventDate);
      const year = date.getFullYear().toString();
      const month = date.toLocaleString('default', { month: 'long' });

      if (!groups[year]) groups[year] = {};
      if (!groups[year][month]) groups[year][month] = [];
      groups[year][month].push(e);
    }

    return Object.keys(groups).sort((a, b) => b.localeCompare(a)).map(year => {
      const months = Object.keys(groups[year]).sort((a, b) => {
        const monthsOrder = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
        return monthsOrder.indexOf(b) - monthsOrder.indexOf(a);
      }).map(month => ({
        month,
        events: groups[year][month]
      }));
      return { year, months };
    });
  });

  addEvent() {
    this.dialog.open(TimelineDialogComponent, { width: '500px' })
      .afterClosed().subscribe(res => {
        if (res) this.load();
      });
  }

  editEvent(event: TimelineEvent) {
    this.dialog.open(TimelineDialogComponent, { data: { event }, width: '500px' })
      .afterClosed().subscribe(res => {
        if (res) this.load();
      });
  }

  deleteEvent(id: string) {
    if (!confirm('Are you sure you want to delete this event?')) return;
    this.timelineService.deleteEvent(id).subscribe({
      next: () => {
        this.snackBar.open('Event deleted successfully', 'Close', { duration: 3000 });
        this.load();
      }
    });
  }

  getBadgeClass(cat: string): string {
    const c = cat.toLowerCase();
    switch (c) {
      case 'career': return 'badge-career';
      case 'education': return 'badge-education';
      case 'health': return 'badge-health';
      case 'finance': return 'badge-finance';
      case 'travel': return 'badge-travel';
      case 'personal': return 'badge-personal';
      default: return 'badge-other';
    }
  }
}