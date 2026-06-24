import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { BehaviorSubject, Observable, throwError } from "rxjs";
import { map, catchError, tap } from 'rxjs/operators';
import { ApiResponse, AuthResponse, User } from "../models/user.model";
import { CryptoService } from "./crypto.service";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private http = inject(HttpClient)
    private apiUrl = `${environment.apiUrl}/auth`;

    private cryptoService = inject(CryptoService);

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
        return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, credentials, { withCredentials: true })
            .pipe(
                map(res => res.data),
                tap(res => {
                    this.accessToken$.next(res.accessToken || null);
                    this.currentUserSubject$.next({ email: res.email });
                    if (res.email) {
                        localStorage.setItem('user_email', res.email);
                    }

                    if (res.encryptionSalt){
                        this.cryptoService.initializeSession(credentials.password, res.encryptionSalt);
                    }
                })
            );
    }

    register(data: { email: string; password: string }): Observable<AuthResponse> {
        return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, data)
            .pipe(
                map(res => res.data)
            );
    }

    logout(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
            tap(() => this.clearSession()),
            catchError(err => {
                this.clearSession();
                return throwError(() => err);
            })
        );
    }

    refreshToken(): Observable<string> {
        return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/refresh`, {}, { withCredentials: true }).pipe(
            map(res => {
                const token = res.data?.accessToken;
                if (!token) {
                    throw new Error('Refresh failed: access token missing from response.');
                }
                this.accessToken$.next(token);
                if (res.data?.email) {
                    this.currentUserSubject$.next({ email: res.data.email });
                    localStorage.setItem('user_email', res.data.email);
                }
                return token;
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
    localStorage.removeItem('user_email');
  }

}




