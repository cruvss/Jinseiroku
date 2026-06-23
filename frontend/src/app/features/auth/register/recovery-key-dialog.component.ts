import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule
} from '@angular/material/dialog';

import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-recovery-key-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatIconModule,
    MatCheckboxModule,
    MatButtonModule
  ],
  template: `
    <div class="recovery-dialog">
      <h2 mat-dialog-title class="gradient-text">
        ⚠️ Important: Save Your Recovery Key
      </h2>

      <mat-dialog-content>
        <p>
          This is your <strong>one-time recovery key</strong>. You will need it
          if you forget your password.
        </p>

        <p class="warning">
          Without this key, your encrypted data will be permanently lost!
        </p>

        <div class="recovery-key-box">
          <code>{{ data.recoveryKey }}</code>
        </div>

        <div class="actions">
          <button
            mat-raised-button
            color="primary"
            type="button"
            (click)="copyKey()">
            <mat-icon>content_copy</mat-icon>
            Copy Key
          </button>

          <button
            mat-raised-button
            color="accent"
            type="button"
            (click)="downloadKey()">
            <mat-icon>download</mat-icon>
            Download
          </button>
        </div>

        <mat-checkbox [(ngModel)]="confirmed" color="primary">
          I have saved my recovery key securely
        </mat-checkbox>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button
          mat-raised-button
          color="warn"
          [disabled]="!confirmed"
          (click)="close()">
          I Understand, Continue
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .recovery-dialog {
      padding: 1rem;
    }

    .warning {
      color: #ff6b6b;
      font-weight: 500;
      margin: 1rem 0;
    }

    .recovery-key-box {
      background: rgba(0, 0, 0, 0.3);
      padding: 1.5rem;
      border-radius: 8px;
      margin: 1.5rem 0;
      text-align: center;
      border: 2px solid #3f51b5;
    }

    .recovery-key-box code {
      font-size: 1.2rem;
      font-family: 'Courier New', monospace;
      color: #00bcd4;
      letter-spacing: 2px;
      word-break: break-all;
    }

    .actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      margin: 1.5rem 0;
    }

    .actions button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    mat-checkbox {
      display: block;
      margin-top: 1rem;
    }

    mat-dialog-actions {
      padding-top: 1rem;
    }
  `]
})
export class RecoveryKeyDialogComponent {
  confirmed = false;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { recoveryKey: string },
    private dialogRef: MatDialogRef<RecoveryKeyDialogComponent>
  ) {}

  async copyKey(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.data.recoveryKey);
    } catch (error) {
      console.error('Failed to copy recovery key', error);
    }
  }

  downloadKey(): void {
    const blob = new Blob(
      [this.data.recoveryKey],
      { type: 'text/plain;charset=utf-8' }
    );

    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = 'jensiroku-recovery-key.txt';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    URL.revokeObjectURL(url);
  }

  close(): void {
    this.dialogRef.close(true);
  }
}