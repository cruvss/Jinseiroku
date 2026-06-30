import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ScheduledNotification } from '../models/reminder.model';
import { ApiResponse } from '../models/user.model';
import { Observable, map, tap } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class ReminderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reminders`;
  reminders = signal<ScheduledNotification[]>([]);
  unreadCount = signal(0);
  loadUpcoming(): Observable<ScheduledNotification[]> {
    return this.http.get<ApiResponse<ScheduledNotification[]>>(`${this.apiUrl}/upcoming`).pipe(
      map(res => res.data),
      tap(data => {
        this.reminders.set(data);
        const now = new Date();
        const unread = data.filter(r => new Date(r.scheduledFor) <= now && r.status === 'sent');
        this.unreadCount.set(unread.length);
      })
    );
  }
  dismiss(id: string): Observable<void> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/dismiss`, {}).pipe(
      map(() => void 0),
      tap(() => this.loadUpcoming().subscribe())
    );
  }
  snooze(id: string, days: number): Observable<void> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/snooze?days=${days}`, {}).pipe(
      map(() => void 0),
      tap(() => this.loadUpcoming().subscribe())
    );
  }
}