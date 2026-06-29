import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CryptoService } from './crypto.service';
import { TimelineEvent } from '../models/timeline.model';
import { ApiResponse } from '../models/user.model';
import { map, Observable, switchMap, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TimelineService {
  private http = inject(HttpClient);
  private crypto = inject(CryptoService);
  private apiUrl = `${environment.apiUrl}/timeline`;

  events = signal<TimelineEvent[]>([]);

  loadEvents(): Observable<TimelineEvent[]> {
    return this.http.get<ApiResponse<TimelineEvent[]>>(this.apiUrl).pipe(
      map(res => res.data),
      switchMap(async events => {
        const decryptedEvents = [];
        for (const e of events) {
          e.decryptedTitle = await this.decryptField(e.titleEncrypted);
          if (e.descriptionEncrypted) {
            e.decryptedDescription = await this.decryptField(e.descriptionEncrypted);
          }
          decryptedEvents.push(e);
        }
        return decryptedEvents;
      }),
      tap(decrypted => this.events.set(decrypted))
    );
  }

  createEvent(event: TimelineEvent, plainTitle: string, plainDescription?: string): Observable<TimelineEvent> {
    return this.encryptEventFields(plainTitle, plainDescription).pipe(
      switchMap(enc => {
        const payload: TimelineEvent = {
          ...event,
          titleEncrypted: enc.title,
          descriptionEncrypted: enc.desc
        };
        return this.http.post<ApiResponse<TimelineEvent>>(this.apiUrl, payload);
      }),
      map(res => res.data),
      tap(() => this.loadEvents().subscribe())
    );
  }

  updateEvent(id: string, event: TimelineEvent, plainTitle: string, plainDescription?: string): Observable<TimelineEvent> {
    return this.encryptEventFields(plainTitle, plainDescription).pipe(
      switchMap(enc => {
        const payload: TimelineEvent = {
          ...event,
          titleEncrypted: enc.title,
          descriptionEncrypted: enc.desc
        };
        return this.http.put<ApiResponse<TimelineEvent>>(`${this.apiUrl}/${id}`, payload);
      }),
      map(res => res.data),
      tap(() => this.loadEvents().subscribe())
    );
  }

  deleteEvent(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(
      map(() => void 0),
      tap(() => this.loadEvents().subscribe())
    );
  }

  private encryptEventFields(title: string, desc?: string): Observable<{ title: string; desc?: string }> {
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