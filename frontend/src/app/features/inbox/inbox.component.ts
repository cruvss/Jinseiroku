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
  templateUrl:'inbox.component.html',
  styleUrl: 'inbox.component.scss'
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

      let contentText = text;
      if (!contentText && this.selectedFile) {
        contentText = this.selectedFile.name;
      }

      if (contentText) {
        const encText = await this.crypto.encryptText(contentText, dek);
        fd.append('textContentEncrypted', `${encText.iv}:${encText.ciphertext}`);
      }
      if (this.selectedFile) {
        const fileBytes = await this.selectedFile.arrayBuffer();
        const encFile = await this.crypto.encryptFile(fileBytes, dek);
        // Pass original MIME type so backend can store it instead of octet-stream
        const encryptedBlob = new Blob([encFile.iv as BufferSource, encFile.ciphertext], { type: this.selectedFile.type || 'application/octet-stream' });
        fd.append('file', encryptedBlob, 'file.bin');
      }

      this.inboxService.capture(fd).subscribe(() => {
        this.captureForm.reset();
        this.selectedFile = null;
        this.isCapturing.set(false);
        this.inboxService.unreadCount.update(n => n + 1);
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

  deleteItem(id: string) { 
    this.inboxService.delete(id).subscribe(() => {
      this.inboxService.unreadCount.update(n => Math.max(0, n - 1));
      this.load();
    }); 
  }
  
  triageVault(item: InboxItem) {
    this.dialog.open(StagingVaultDialogComponent, { data: item, width: '400px' })
      .afterClosed().subscribe(res => { 
        if (res) {
          this.inboxService.unreadCount.update(n => Math.max(0, n - 1));
          this.load();
        }
      });
  }

  getIconName(item: InboxItem): string {
    if (item.contentType === 'TEXT') return 'description';
    if (item.mimeType?.startsWith('image/')) return 'image';
    if (item.mimeType === 'application/pdf') return 'picture_as_pdf';
    return 'insert_drive_file';
  }

  getIconClass(item: InboxItem): string {
    if (item.contentType === 'TEXT') return 'icon-text';
    if (item.mimeType?.startsWith('image/')) return 'icon-image';
    if (item.mimeType === 'application/pdf') return 'icon-pdf';
    return 'icon-default';
  }

  getItemTitle(item: InboxItem): string {
    if (item.decryptedText) return item.decryptedText;
    if (item.mimeType) return `File attachment (${item.mimeType.split('/')[1] || item.mimeType})`;
    return 'Encrypted Item';
  }
}