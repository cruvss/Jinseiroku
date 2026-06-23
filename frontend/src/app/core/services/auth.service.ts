import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { BehaviorSubject, Observable, throwError } from "rxjs";
import { map, catchError, tap } from 'rxjs/operators';
import { AuthResponse, User } from "../models/user.model";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private http = inject(HttpClient)
    private apiUrl = `${environment.apiUrl}/auth`;

    private accessToken$ = new BehaviorSubject<string | null>(null);
    private currentUserSubject$ = new BehaviorSubject<User | null>(null);

    public readonly isAuthenticated$ = this.accessToken$.pipe(map(token => !!token));
    public readonly currentUser$ = this.currentUserSubject$.asObservable();

    get token(): string | null {
        return this.accessToken$.value;
    }
    get user(): User | null {
        return this.currentUserSubject$.value;
    }

    login(credentials: { email: string; password: string }): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials, { withCredentials: true })
            .pipe(tap(res => {
                this.accessToken$.next(res.accessToken);
                this.currentUserSubject$.next(res.user);
            }));
    }

    register(data: { email: string; passwordHash: string; encryptionSalt: string }): Observable<{ user: User; recoveryKey: string }> {
        return this.http.post<{ user: User; recoveryKey: string }>
            (`${this.apiUrl}/register`, data);
    }

    logout(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true })
    }

    refreshToken(): Observable<string> {
        return this.http.post<{ accessToken: string }>(`${this.apiUrl}/refresh`, {}, { withCredentials: true }).pipe(
        map(res => {
            this.accessToken$.next(res.accessToken);
            return res.accessToken;
        }),
        catchError(err => {
            this.clearSession();
            return throwError(() => err);
        })
        );
    }

    clearSession(): void {
    this.accessToken$.next(null);
    this.currentUserSubject$.next(null);
  }

}




