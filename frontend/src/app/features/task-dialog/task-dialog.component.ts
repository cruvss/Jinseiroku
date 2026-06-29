import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, provideNativeDateAdapter } from '@angular/material/core';
import { TaskService } from '../../core/services/task.service';
import { Task } from '../../core/models/task.model';

@Component({
  selector: 'app-task-dialog',
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
  templateUrl: './task-dialog.component.html',
  styleUrl: './task-dialog.component.scss'
})
export class TaskDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<TaskDialogComponent>);
  private taskService = inject(TaskService);
  public data: { task?: Task } = inject(MAT_DIALOG_DATA) || {};

  categories = ['Home', 'Health', 'Finance', 'Work', 'Auto', 'Personal', 'Others'];
  cycleTypes = ['DAYS', 'WEEKS', 'MONTHS', 'YEARS'];

  // Hardcoded templates configuration
  templates = [
    { label: 'Dentist Checkup (6 Months)', title: 'Dentist Checkup', category: 'Health', isRecurring: true, cycleInterval: 6, cycleType: 'MONTHS' },
    { label: 'HVAC Filter Change (3 Months)', title: 'Change HVAC Air Filter', category: 'Home', isRecurring: true, cycleInterval: 3, cycleType: 'MONTHS' },
    { label: 'Car Service (6 Months)', title: 'Car General Service & Oil Change', category: 'Auto', isRecurring: true, cycleInterval: 6, cycleType: 'MONTHS' },
    { label: 'Smoke Detector Test (6 Months)', title: 'Test Smoke Alarms', category: 'Home', isRecurring: true, cycleInterval: 6, cycleType: 'MONTHS' }
  ];

  form = this.fb.group({
    title: ['', Validators.required],
    description: [''],
    category: ['Personal', Validators.required],
    dueDate: ['', Validators.required],
    isRecurring: [false],
    cycleInterval: [1],
    cycleType: ['MONTHS'],
  });

  ngOnInit() {
    this.form.get('isRecurring')?.valueChanges.subscribe(isRec => {
      this.updateCycleValidators(isRec || false);
    });

    if (this.data?.task) {
      const t = this.data.task;
      this.form.patchValue({
        title: t.decryptedTitle || '',
        description: t.decryptedDescription || '',
        category: t.category,
        dueDate: t.dueDate ? new Date(t.dueDate).toISOString() : '',
        isRecurring: t.isRecurring,
        cycleInterval: t.cycleInterval || 1,
        cycleType: t.cycleType || 'MONTHS'
      });
      this.updateCycleValidators(t.isRecurring);
    }
  }

  applyTemplate(tpl: any) {
    this.form.patchValue({
      title: tpl.title,
      category: tpl.category,
      isRecurring: tpl.isRecurring,
      cycleInterval: tpl.cycleInterval,
      cycleType: tpl.cycleType
    });
  }

  updateCycleValidators(isRec: boolean) {
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
  }

  submit() {
    if (this.form.invalid) return;

    const val = this.form.value;
    const isoDateString = val.dueDate ? new Date(val.dueDate).toISOString().split('T')[0] : undefined;

    const payload: Task = {
      titleEncrypted: '', // Encrypted inside taskService
      category: val.category!,
      isRecurring: val.isRecurring || false,
      cycleInterval: val.isRecurring ? val.cycleInterval! : undefined,
      cycleType: val.isRecurring ? (val.cycleType! as any) : undefined,
      dueDate: isoDateString,
      status: this.data.task ? this.data.task.status : 'pending'
    };

    if (this.data.task?.id) {
      this.taskService.updateTask(this.data.task.id, payload, val.title!, val.description || '').subscribe(() => {
        this.dialogRef.close(true);
      });
    } else {
      this.taskService.createTask(payload, val.title!, val.description || '').subscribe(() => {
        this.dialogRef.close(true);
      });
    }
  }
}