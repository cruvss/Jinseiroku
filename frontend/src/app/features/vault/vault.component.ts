import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CryptoService } from '../../core/services/crypto.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { ApiResponse, VaultParamsResponse } from '../../core/models/user.model';

interface DocumentMetadata {
  id: string;
  fileNameEncrypted: string;
  category: string;
  encryptedDek: string;
  fileSizeBytes: number;
  mimeType: string;
  expiryDate?: string;
  // decrypted locally
  decryptedName?: string;
}

@Component({
  selector: 'app-vault',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: 'vault.component.html',
  styleUrls: ['vault.component.scss']
})
export class VaultComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  protected cryptoService = inject(CryptoService);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  // Session Unlock Password Form
  unlockForm: FormGroup = this.fb.group({
    password: ['', [Validators.required]]
  });

  // Upload Form
  uploadForm: FormGroup = this.fb.group({
    category: ['Documents', Validators.required],
    expiryDate: [null],
    notes: ['']
  });

  selectedFile: File | null = null;
  documents = signal<DocumentMetadata[]>([]);
  isLoading = signal(false);
  isUploading = signal(false);
  categories = ['Documents', 'Credentials', 'Finance', 'Personal', 'Others'];

  ngOnInit() {
    if (this.cryptoService.hasActiveSession()) {
      this.loadDocuments();
    }
  }

  // Prompt user to unlock Vault (derive KEK) if reloaded or session cleared
  async onUnlock() {
    if (this.unlockForm.invalid) return;
    this.isLoading.set(true);
    try {
      const email = localStorage.getItem('user_email') || ''; 
      this.http.get<ApiResponse<VaultParamsResponse>>(`${environment.apiUrl}/auth/salt?email=${email}`).subscribe({
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
          } else {
            // Legacy / first unlock: auto-generate and save the verification token
            try {
              const verification = await this.cryptoService.generateKekVerification();
              this.authService.saveKekVerification(verification).subscribe({
                next: () => console.log('KEK verification generated and saved successfully for legacy/first unlock'),
                error: (err) => console.error('Failed to save KEK verification for legacy/first unlock', err)
              });
            } catch (e) {
              console.error('Failed to auto-generate KEK verification', e);
            }
          }
          
          this.loadDocuments();
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

  onFileSelected(e: Event) {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  async onUpload() {
    if (!this.selectedFile || !this.cryptoService.hasActiveSession()) return;
    this.isUploading.set(true);

    try {
      // 1. Generate DEK (Data Encryption Key)
      const dek = await this.cryptoService.generateDEK();

      // 2. Encrypt File Bytes
      const fileBytes = await this.selectedFile.arrayBuffer();
      const encryptedFile = await this.cryptoService.encryptFile(fileBytes, dek);

      // 3. Encrypt Metadata (File Name)
      // We prepend the IV to the filename ciphertext, or store it in a standard envelope
      const encryptedNameObj = await this.cryptoService.encryptText(this.selectedFile.name, dek);
      const namePayload = `${encryptedNameObj.iv}:${encryptedNameObj.ciphertext}`;

      // 4. Wrap DEK with session KEK (Argon2 derived)
      const wrappedDek = await this.cryptoService.wrapDEK(dek);

      // 5. Build Multipart Request
      const formData = new FormData();
      const blob = new Blob([encryptedFile.ciphertext], { type: 'application/octet-stream' });
      
      const ivBytes = encryptedFile.iv;
      const combinedBlob = new Blob([ivBytes as BufferSource, encryptedFile.ciphertext], { type: 'application/octet-stream' });

      formData.append('file', combinedBlob, 'encrypted.bin');
      formData.append('fileNameEncrypted', namePayload);
      formData.append('category', this.uploadForm.value.category);
      formData.append('encryptedDek', wrappedDek);
      if (this.uploadForm.value.expiryDate) {
        formData.append('expiryDate', this.uploadForm.value.expiryDate.toISOString().split('T')[0]);
      }

      this.http.post(`${environment.apiUrl}/vault/documents`, formData).subscribe({
        next: () => {
          this.isUploading.set(false);
          this.selectedFile = null;
          this.uploadForm.reset({ category: 'Documents' });
          this.loadDocuments();
        },
        error: (err) => {
          console.error(err);
          this.isUploading.set(false);
          alert('Upload failed.');
        }
      });

    } catch (e) {
      console.error(e);
      this.isUploading.set(false);
      alert('Encryption/Upload process failed.');
    }
  }

  loadDocuments() {
    this.isLoading.set(true);
    this.http.get<{ data: { content: DocumentMetadata[] } }>(`${environment.apiUrl}/vault/documents`).subscribe({
      next: async (res) => {
        const docs = res.data.content;
        for (const doc of docs) {
          try {
            // Unwrap DEK using KEK
            const dek = await this.cryptoService.unwrapDEK(doc.encryptedDek);

            // Decrypt File Name
            const [ivBase64, nameCipherBase64] = doc.fileNameEncrypted.split(':');
            doc.decryptedName = await this.cryptoService.decryptText(nameCipherBase64, dek, ivBase64);
          } catch (e) {
            doc.decryptedName = '[Decryption Failed]';
          }
        }
        this.documents.set(docs);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  async downloadFile(doc: DocumentMetadata) {
    this.isLoading.set(true);
    this.http.get(`${environment.apiUrl}/vault/documents/${doc.id}/download`, { responseType: 'arraybuffer' }).subscribe({
      next: async (buffer) => {
        try {
          // 1. Unwrap DEK
          const dek = await this.cryptoService.unwrapDEK(doc.encryptedDek);

          // 2. Extract IV (first 12 bytes of the downloaded buffer) and ciphertext
          const iv = new Uint8Array(buffer.slice(0, 12));
          const ciphertext = buffer.slice(12);

          // 3. Decrypt file
          const decryptedBytes = await this.cryptoService.decryptFile(ciphertext, dek, iv);

          // 4. Trigger browser download
          const blob = new Blob([decryptedBytes], { type: doc.mimeType || 'application/octet-stream' });
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = doc.decryptedName || 'decrypted-file';
          link.click();
          window.URL.revokeObjectURL(url);
        } catch (e) {
          alert('Decryption failed.');
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        alert('Download failed.');
      }
    });
  }

  deleteFile(doc: DocumentMetadata) {
    if (!confirm('Are you sure you want to permanently delete this document?')) return;
    this.isLoading.set(true);
    this.http.delete(`${environment.apiUrl}/vault/documents/${doc.id}`).subscribe({
      next: () => this.loadDocuments(),
      error: () => {
        this.isLoading.set(false);
        alert('Delete failed.');
      }
    });
  }
}