import { Component, Inject, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatIconModule],
  templateUrl:'profile-dialog.component.html',
  styleUrl: 'profile-dialog.component.scss'
})
export class ProfileDialogComponent implements OnInit {
  private http = inject(HttpClient);
  profileData = signal<any>(null);

  ngOnInit() {
    this.http.get<{ data: any }>(`${environment.apiUrl}/auth/me`)
      .subscribe({
        next: (res) => this.profileData.set(res.data),
        error: (err) => console.error('Failed to load profile data', err)
      });
  }
}