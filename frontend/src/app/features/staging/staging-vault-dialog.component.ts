import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CryptoService } from '../../core/services/crypto.service';
import { InboxItem } from '../../core/models/inbox.model';
import { InboxService } from '../../core/services/inbox.service';

@Component({
  selector: 'app-triage-vault',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>Triage to Vault</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="w-100">
          <mat-label>Category</mat-label>
          <mat-select formControlName="category">
            <mat-option value="Documents">Documents</mat-option>
            <mat-option value="Finance">Finance</mat-option>
            <mat-option value="Personal">Personal</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-100">
          <mat-label>Notes (from Inbox text)</mat-label>
          <textarea matInput formControlName="notes" rows="3"></textarea>
        </mat-form-field>
        <p *ngIf="item.contentType !== 'TEXT'"><em>File attachment will be transferred securely.</em></p>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button color="primary" [disabled]="isLoading()" (click)="submit()">Save to Vault</button>
    </mat-dialog-actions>
  `
})
export class StagingVaultDialogComponent {
  item: InboxItem = inject(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef);
  fb = inject(FormBuilder);
  http = inject(HttpClient);
  crypto = inject(CryptoService);
  inboxService = inject(InboxService);
  isLoading = signal(false);

  form = this.fb.group({
    category: ['Documents', Validators.required],
    notes: [this.item.decryptedText || '']
  });

  async submit() {
    this.isLoading.set(true);
    try {
      const dek = await this.crypto.generateDEK();
      const wrappedDek = await this.crypto.wrapDEK(dek);
      const formData = new FormData();
      formData.append('category', this.form.value.category!);
      formData.append('encryptedDek', wrappedDek);

      const encName = await this.crypto.encryptText('Inbox_Triaged_File', dek);
      formData.append('fileNameEncrypted', `${encName.iv}:${encName.ciphertext}`);

      if (this.form.value.notes) {
        const encNotes = await this.crypto.encryptText(this.form.value.notes, dek);
        formData.append('notesEncrypted', `${encNotes.iv}:${encNotes.ciphertext}`);
      }

      if (this.item.contentType !== 'TEXT') {
        const buffer = await this.inboxService.download(this.item.id).toPromise();
        // The inbox file is ALREADY encrypted! 
        // For standard simplicity without re-encryption overhead in the browser:
        // Decrypt with inbox DEK, re-encrypt with Vault DEK.
        const inboxDek = await this.crypto.unwrapDEK(this.item.encryptedDek!);
        const iv = new Uint8Array(buffer!.slice(0, 12));
        const cipher = buffer!.slice(12);
        const plainBytes = await this.crypto.decryptFile(cipher, inboxDek, iv);
        
        const newEncFile = await this.crypto.encryptFile(plainBytes, dek);
        const combined = new Blob([newEncFile.iv as BufferSource, newEncFile.ciphertext], { type: 'application/octet-stream' });
        formData.append('file', combined, 'encrypted.bin');
      } else {
        // Mock file for vault if only text was captured
        const dummy = new Blob(['no-file'], {type: 'text/plain'});
        formData.append('file', dummy, 'dummy.txt'); 
      }

      const res = await this.http.post<{data: {id: string}}>(`${environment.apiUrl}/vault/documents`, formData).toPromise();
      await this.inboxService.markStaged(this.item.id, 'vault', res!.data.id).toPromise();
      
      this.dialogRef.close(true);
    } catch (e) {
      alert('Triage failed');
      this.isLoading.set(false);
    }
  }
}