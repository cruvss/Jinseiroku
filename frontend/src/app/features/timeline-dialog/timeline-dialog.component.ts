import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, provideNativeDateAdapter } from '@angular/material/core';
import { TimelineService } from '../../core/services/timeline.service';
import { CryptoService } from '../../core/services/crypto.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { TimelineEvent, TimelineDialogData } from '../../core/models/timeline.model';

@Component({
  selector: 'app-timeline-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './timeline-dialog.component.html',
  styleUrl: './timeline-dialog.component.scss'
})
export class TimelineDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<TimelineDialogComponent>);
  private timelineService = inject(TimelineService);
  private crypto = inject(CryptoService);
  private http = inject(HttpClient);
  public data: TimelineDialogData = inject(MAT_DIALOG_DATA);

  categories = ['Career', 'Education', 'Health', 'Finance', 'Travel', 'Personal', 'Others'];
  vaultDocuments: { id: string; name: string }[] = [];

  form = this.fb.group({
    title: ['', Validators.required],
    description: [''],
    eventDate: ['', Validators.required],
    endDate: [''],
    category: ['Personal', Validators.required],
    linkedDocumentIds: [<string[]>[]]
  });

  isEdit = false;

  ngOnInit() {
    if (this.data && this.data.event) {
      this.isEdit = true;
      const event = this.data.event;
      this.form.patchValue({
        title: event.decryptedTitle || '',
        description: event.decryptedDescription || '',
        eventDate: event.eventDate,
        endDate: event.endDate || '',
        category: event.category,
        linkedDocumentIds: event.linkedDocumentIds || []
      });
    } else if (this.data && this.data.inboxText) {
      this.form.patchValue({
        description: this.data.inboxText
      });
    }

    if (this.crypto.hasActiveSession()) {
      this.loadVaultDocuments();
    }
  }

  loadVaultDocuments() {
    this.http.get<{ data: { content: any[] } }>(`${environment.apiUrl}/vault/documents`).subscribe({
      next: async (res) => {
        const docs = res.data.content;
        const decryptedList = [];
        for (const doc of docs) {
          try {
            const dek = await this.crypto.unwrapDEK(doc.encryptedDek);
            const [ivBase64, nameCipherBase64] = doc.fileNameEncrypted.split(':');
            const name = await this.crypto.decryptText(nameCipherBase64, dek, ivBase64);
            decryptedList.push({ id: doc.id, name });
          } catch (e) {
            decryptedList.push({ id: doc.id, name: '[Decryption Failed]' });
          }
        }
        this.vaultDocuments = decryptedList;
      }
    });
  }

  submit() {
    if (this.form.invalid) return;

    const val = this.form.value;
    const eventDateStr = val.eventDate ? new Date(val.eventDate).toISOString().split('T')[0] : '';
    const endDateStr = val.endDate ? new Date(val.endDate).toISOString().split('T')[0] : undefined;

    const payload: TimelineEvent = {
      titleEncrypted: '',
      eventDate: eventDateStr,
      endDate: endDateStr,
      category: val.category!,
      linkedDocumentIds: val.linkedDocumentIds || []
    };

    if (this.isEdit) {
      this.timelineService.updateEvent(this.data.event!.id!, payload, val.title!, val.description || '').subscribe({
        next: () => this.dialogRef.close(true)
      });
    } else {
      this.timelineService.createEvent(payload, val.title!, val.description || '').subscribe({
        next: () => this.dialogRef.close(true)
      });
    }
  }
}