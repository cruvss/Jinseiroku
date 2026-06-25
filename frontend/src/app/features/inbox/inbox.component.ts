import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { InboxService } from '../../core/services/inbox.service';
import { CryptoService } from '../../core/services/crypto.service';
import { InboxItem } from '../../core/models/inbox.model';
import { StagingVaultDialogComponent } from '../staging/staging-vault-dialog.component';

@Component({
  selector: 'app-inbox',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  template: `
    <div class="inbox-container">
      <form class="capture-bar card" [formGroup]="captureForm" (submit)="onCapture()">
        <input type="file" #fileInput hidden (change)="onFileSelected($event)" />
        <button type="button" class="icon-btn" (click)="fileInput.click()"><mat-icon>attach_file</mat-icon></button>
        <span *ngIf="selectedFile" class="file-badge">{{selectedFile.name}}</span>
        <input type="text" formControlName="text" placeholder="Type a thought..." />
        <button type="submit" [disabled]="isCapturing()">Capture</button>
      </form>

      <div class="inbox-list">
        <div class="card item-card" *ngFor="let item of items()">
          <div class="item-content">
            <mat-icon *ngIf="item.contentType !== 'TEXT'">insert_drive_file</mat-icon>
            <p>{{ item.decryptedText || 'No text attached' }}</p>
            <small>Captured {{ item.capturedAt | date:'short' }}</small>
          </div>
          <div class="item-actions">
            <button (click)="triageVault(item)">→ Vault</button>
            <button (click)="deleteItem(item.id)">🗑️</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inbox-container { max-width: 800px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
    .capture-bar { display: flex; align-items: center; gap: 10px; padding: 10px 20px; border-radius: 50px; }
    .capture-bar input[type="text"] { flex: 1; border: none; outline: none; background: transparent; font-size: 16px; }
    .item-card { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
    .item-actions button { margin-left: 10px; border: none; background: #eee; padding: 5px 15px; border-radius: 20px; cursor: pointer; }
    .item-actions button:hover { background: #e0e0e0; }
  `]
})
export class InboxComponent implements OnInit {
  inboxService = inject(InboxService);
  crypto = inject(CryptoService);
  dialog = inject(MatDialog);
  fb = inject(FormBuilder);

  items = signal<InboxItem[]>([]);
  selectedFile: File | null = null;
  isCapturing = signal(false);
  captureForm = this.fb.group({ text: [''] });

  ngOnInit() { this.load(); }

  onFileSelected(e: any) { this.selectedFile = e.target.files[0]; }

  async onCapture() {
    const text = this.captureForm.value.text?.trim();
    if (!text && !this.selectedFile) return;
    this.isCapturing.set(true);
    
    try {
      const dek = await this.crypto.generateDEK();
      const fd = new FormData();
      fd.append('encryptedDek', await this.crypto.wrapDEK(dek));

      if (text) {
        const encText = await this.crypto.encryptText(text, dek);
        fd.append('textContentEncrypted', `${encText.iv}:${encText.ciphertext}`);
      }
      if (this.selectedFile) {
        const fileBytes = await this.selectedFile.arrayBuffer();
        const encFile = await this.crypto.encryptFile(fileBytes, dek);
        fd.append('file', new Blob([encFile.iv as BufferSource, encFile.ciphertext]), 'file.bin');
      }

      this.inboxService.capture(fd).subscribe(() => {
        this.captureForm.reset();
        this.selectedFile = null;
        this.isCapturing.set(false);
        this.load();
      });
    } catch (e) { this.isCapturing.set(false); }
  }

  load() {
    this.inboxService.list().subscribe(async (res) => {
      const docs = res.data;
      for (const d of docs) {
        if (d.textContentEncrypted) {
          try {
            const dek = await this.crypto.unwrapDEK(d.encryptedDek!);
            const [iv, cipher] = d.textContentEncrypted.split(':');
            d.decryptedText = await this.crypto.decryptText(cipher, dek, iv);
          } catch { d.decryptedText = '[Decryption Failed]'; }
        }
      }
      this.items.set(docs);
    });
  }

  deleteItem(id: string) { this.inboxService.delete(id).subscribe(() => this.load()); }
  triageVault(item: InboxItem) {
    this.dialog.open(StagingVaultDialogComponent, { data: item, width: '400px' })
      .afterClosed().subscribe(res => { if (res) this.load(); });
  }
}