import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TaskService } from '../../core/services/task.service';
import { Task } from '../../core/models/task.model';
import { TaskDialogComponent } from '../task-dialog/task-dialog.component';

@Component({
  selector: 'app-task',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './task.component.html',
  styleUrl: './task.component.scss'
})
export class TaskComponent implements OnInit {
  taskService = inject(TaskService);
  dialog = inject(MatDialog);
  snackBar = inject(MatSnackBar);

  ngOnInit() {
    this.taskService.loadTasks().subscribe();
  }

  isOverdue(task: Task): boolean {
    if (!task.dueDate) return false;
    const due = new Date(task.dueDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return due < today;
  }

  openAddTaskDialog() {
    this.dialog.open(TaskDialogComponent, { width: '500px' })
      .afterClosed().subscribe(res => {
        if (res) this.snackBar.open('Task created!', 'Close', { duration: 3000 });
      });
  }

  openEditTaskDialog(task: Task) {
    this.dialog.open(TaskDialogComponent, { data: { task }, width: '500px' })
      .afterClosed().subscribe(res => {
        if (res) this.snackBar.open('Task updated!', 'Close', { duration: 3000 });
      });
  }

  toggleTodoComplete(task: Task) {
    if (!task.id) return;
    this.taskService.completeTask(task.id).subscribe({
      next: () => this.snackBar.open('To-do completed!', 'Close', { duration: 3000 }),
      error: () => this.snackBar.open('Failed to complete task.', 'Close', { duration: 3000 })
    });
  }

  markRecurringDone(task: Task) {
    if (!task.id) return;
    const notes = prompt("Add optional encrypted completion notes:");
    this.taskService.completeTask(task.id, notes || '').subscribe({
      next: () => this.snackBar.open('Recurring task completed & rescheduled!', 'Close', { duration: 3000 }),
      error: () => this.snackBar.open('Failed to register task completion.', 'Close', { duration: 3000 })
    });
  }

  deleteTask(task: Task) {
    if (!task.id || !confirm('Permanently delete this task?')) return;
    this.taskService.deleteTask(task.id).subscribe({
      next: () => this.snackBar.open('Task deleted.', 'Close', { duration: 3000 })
    });
  }
}