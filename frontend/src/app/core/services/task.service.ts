import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CryptoService } from './crypto.service';
import { Task, TaskCompletion } from '../models/task.model';
import { ApiResponse } from '../models/user.model';
import { forkJoin, map, Observable, switchMap, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private http = inject(HttpClient);
  private crypto = inject(CryptoService);
  private apiUrl = `${environment.apiUrl}/tasks`;

  tasks = signal<Task[]>([]);

  // Derived computed filters
  oneOffTodos = computed(() => this.tasks().filter(t => !t.isRecurring && t.status === 'pending'));
  recurringTasks = computed(() => this.tasks().filter(t => t.isRecurring && t.status === 'pending'));

  loadTasks(): Observable<Task[]> {
    return this.http.get<ApiResponse<Task[]>>(this.apiUrl).pipe(
      map(res => res.data),
      switchMap(async tasks => {
        const decryptedTasks = [];
        for (const t of tasks) {
          try {
            const dek = await this.crypto.unwrapDEK(t.titleEncrypted.split(':')[2] || ''); // fallback or proper retrieval
          } catch(e) {}
          
          // Decrypt title/description envelope or simple zero-knowledge format
          t.decryptedTitle = await this.decryptField(t.titleEncrypted);
          if (t.descriptionEncrypted) {
            t.decryptedDescription = await this.decryptField(t.descriptionEncrypted);
          }
          decryptedTasks.push(t);
        }
        return decryptedTasks;
      }),
      tap(decrypted => this.tasks.set(decrypted))
    );
  }

  createTask(task: Task, plainTitle: string, plainDescription?: string): Observable<Task> {
    return this.encryptTaskFields(plainTitle, plainDescription).pipe(
      switchMap(enc => {
        const payload: Task = {
          ...task,
          titleEncrypted: enc.title,
          descriptionEncrypted: enc.desc
        };
        return this.http.post<ApiResponse<Task>>(this.apiUrl, payload);
      }),
      map(res => res.data),
      tap(() => this.loadTasks().subscribe())
    );
  }

  updateTask(id: string, task: Task, plainTitle: string, plainDescription?: string): Observable<Task> {
    return this.encryptTaskFields(plainTitle, plainDescription).pipe(
      switchMap(enc => {
        const payload: Task = {
          ...task,
          titleEncrypted: enc.title,
          descriptionEncrypted: enc.desc
        };
        return this.http.put<ApiResponse<Task>>(`${this.apiUrl}/${id}`, payload);
      }),
      map(res => res.data),
      tap(() => this.loadTasks().subscribe())
    );
  }

  deleteTask(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(
      map(() => void 0),
      tap(() => this.loadTasks().subscribe())
    );
  }

  completeTask(id: string, plainNotes?: string): Observable<Task> {
    const encryptNotes$ = plainNotes ? this.encryptText(plainNotes) : new Observable<string>(sub => { sub.next(''); sub.complete(); });

    return encryptNotes$.pipe(
      switchMap(encNotes => {
        return this.http.post<ApiResponse<Task>>(`${this.apiUrl}/${id}/complete`, encNotes);
      }),
      map(res => res.data),
      tap(() => this.loadTasks().subscribe())
    );
  }

  getCompletions(id: string): Observable<TaskCompletion[]> {
    return this.http.get<ApiResponse<TaskCompletion[]>>(`${this.apiUrl}/${id}/completions`).pipe(
      map(res => res.data),
      switchMap(async completions => {
        for (const c of completions) {
          if (c.notesEncrypted) {
            c.decryptedNotes = await this.decryptField(c.notesEncrypted);
          }
        }
        return completions;
      })
    );
  }

  // --- CRYPTO UTILITIES ---
  private encryptTaskFields(title: string, desc?: string): Observable<{ title: string; desc?: string }> {
    return new Observable(subscriber => {
      (async () => {
        try {
          const dek = await this.crypto.generateDEK();
          const wrappedDek = await this.crypto.wrapDEK(dek);

          const encTitle = await this.crypto.encryptText(title, dek);
          const encTitlePayload = `${wrappedDek}:${encTitle.iv}:${encTitle.ciphertext}`;

          let encDescPayload = undefined;
          if (desc) {
            const encDesc = await this.crypto.encryptText(desc, dek);
            encDescPayload = `${wrappedDek}:${encDesc.iv}:${encDesc.ciphertext}`;
          }

          subscriber.next({ title: encTitlePayload, desc: encDescPayload });
          subscriber.complete();
        } catch (e) {
          subscriber.error(e);
        }
      })();
    });
  }

  private encryptText(plainText: string): Observable<string> {
    return new Observable(subscriber => {
      (async () => {
        try {
          const dek = await this.crypto.generateDEK();
          const wrappedDek = await this.crypto.wrapDEK(dek);
          const enc = await this.crypto.encryptText(plainText, dek);
          subscriber.next(`${wrappedDek}:${enc.iv}:${enc.ciphertext}`);
          subscriber.complete();
        } catch (e) {
          subscriber.error(e);
        }
      })();
    });
  }

  private async decryptField(payload: string): Promise<string> {
    try {
      const [wrappedDek, iv, ciphertext] = payload.split(':');
      if (!wrappedDek || !iv || !ciphertext) return '[Decryption Missing Parameters]';
      const dek = await this.crypto.unwrapDEK(wrappedDek);
      return await this.crypto.decryptText(ciphertext, dek, iv);
    } catch (e) {
      return '[Decryption Failed]';
    }
  }
}