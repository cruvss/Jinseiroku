import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, provideNativeDateAdapter } from '@angular/material/core';
import { TaskService } from '../../core/services/task.service';
import { InboxService } from '../../core/services/inbox.service';
import { InboxItem } from '../../core/models/inbox.model';
import { Task } from '../../core/models/task.model';

@Component({
  selector: 'app-staging-task-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: 'staging-task-dialog.component.html',
  styleUrl: 'staging-task-dialog.component.scss'
})
export class StagingTaskDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<StagingTaskDialogComponent>);
  private taskService = inject(TaskService);
  private inboxService = inject(InboxService);
  public data: InboxItem = inject(MAT_DIALOG_DATA);

  categories = ['Home', 'Health', 'Finance', 'Work', 'Auto', 'Personal', 'Others'];
  cycleTypes = ['DAYS', 'WEEKS', 'MONTHS', 'YEARS'];

  form = this.fb.group({
    title: ['', Validators.required],
    category: ['Personal', Validators.required],
    dueDate: ['', Validators.required],
    isRecurring: [false],
    cycleInterval: [1],
    cycleType: ['MONTHS'],
  });

  ngOnInit() {
    this.form.get('isRecurring')?.valueChanges.subscribe(isRec => {
      const intervalCtrl = this.form.get('cycleInterval');
      const typeCtrl = this.form.get('cycleType');
      if (isRec) {
        intervalCtrl?.setValidators([Validators.required, Validators.min(1)]);
        typeCtrl?.setValidators([Validators.required]);
      } else {
        intervalCtrl?.clearValidators();
        typeCtrl?.clearValidators();
      }
      intervalCtrl?.updateValueAndValidity();
      typeCtrl?.updateValueAndValidity();
    });

    if (this.data) {
      this.form.patchValue({
        title: this.data.decryptedText || ''
      });
    }
  }

  submit() {
    if (this.form.invalid) return;

    const val = this.form.value;
    const isoDateString = val.dueDate ? new Date(val.dueDate).toISOString().split('T')[0] : undefined;

    const payload: Task = {
      titleEncrypted: '',
      category: val.category!,
      isRecurring: val.isRecurring || false,
      cycleInterval: val.isRecurring ? val.cycleInterval! : undefined,
      cycleType: val.isRecurring ? (val.cycleType! as any) : undefined,
      dueDate: isoDateString,
      status: 'pending'
    };

    // 1. Create the Task
    this.taskService.createTask(payload, val.title!, '').subscribe({
      next: (createdTask) => {
        // 2. Mark inbox item as staged
        this.inboxService.markStaged(this.data.id, 'task', createdTask.id!).subscribe({
          next: () => this.dialogRef.close(true)
        });
      }
    });
  }
}