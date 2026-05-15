import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, tap, retry, timer, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants/api-endpoints';
import { AuthStorageKeys } from '../constants/auth.constants';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, UserInfo } from '../../shared/interfaces';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  private _currentUser = signal<UserInfo | null>(this.loadUserFromStorage());
  readonly currentUser = this._currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this._currentUser() !== null);

  constructor() {
    // 3.3 — Boot hydrate: if SHOP_OWNER loaded from storage without shopId, hydrate now
    const user = this._currentUser();
    if (user && user.role === 'SHOP_OWNER' && (!(user.shopId && user.shopId > 0) || !user.shopName)) {
      this.hydrateShopId(user);
    }
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}${ApiEndpoints.AUTH_LOGIN}`, request)
      .pipe(tap(res => this.handleAuthSuccess(res.result)));
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}${ApiEndpoints.AUTH_REGISTER}`, request)
      .pipe(tap(res => this.handleAuthSuccess(res.result)));
  }

  logout(): void {
    this.http.post(`${environment.apiUrl}${ApiEndpoints.AUTH_LOGOUT}`, {}).subscribe({ error: () => {} });
    this.clearSession();
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(AuthStorageKeys.ACCESS_TOKEN);
  }

  private handleAuthSuccess(auth: AuthResponse): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem(AuthStorageKeys.ACCESS_TOKEN, auth.accessToken);
    const user: UserInfo = {
      userId: auth.userId,
      email: auth.email,
      fullName: '',
      role: auth.role,
    };
    localStorage.setItem(AuthStorageKeys.CURRENT_USER, JSON.stringify(user));
    this._currentUser.set(user);

    // 3.1 — After login/register, hydrate shopId for SHOP_OWNER
    if (user.role === 'SHOP_OWNER') {
      this.hydrateShopId(user);
    }
  }

  private clearSession(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    // 3.4 — Removing CURRENT_USER entirely also removes shopId (no extra step needed)
    localStorage.removeItem(AuthStorageKeys.ACCESS_TOKEN);
    localStorage.removeItem(AuthStorageKeys.CURRENT_USER);
    this._currentUser.set(null);
  }

  private loadUserFromStorage(): UserInfo | null {
    if (typeof localStorage === 'undefined') return null;
    try {
      const raw = localStorage.getItem(AuthStorageKeys.CURRENT_USER);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  /**
   * 3.2 — Hydrate shopId for SHOP_OWNER by calling GET /v1/shops/my-shop.
   * Retries up to 3 times on 5xx / network errors with delays 2s, 4s, 8s.
   * Does NOT retry on 404, 401, or 403.
   * On failure, leaves shopId as undefined and does NOT call logout().
   */
  private hydrateShopId(user: UserInfo): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.http
      .get<ApiResponse<{ id: number; [key: string]: unknown }>>(
        `${environment.apiUrl}${ApiEndpoints.SHOPS_MY_SHOP}`
      )
      .pipe(
        retry({
          count: 3,
          delay: (error: HttpErrorResponse, attemptIndex: number) => {
            // Do not retry on client errors (4xx)
            if (error instanceof HttpErrorResponse && error.status >= 400 && error.status < 500) {
              return throwError(() => error);
            }
            // Retry on 5xx or network errors (status 0) with exponential backoff
            const delayMs = Math.pow(2, attemptIndex) * 2000; // 2000, 4000, 8000
            return timer(delayMs);
          },
        })
      )
      .subscribe({
        next: res => {
          const shopId = res?.result?.id;
          const shopName = typeof res?.result?.['shopName'] === 'string' ? res.result['shopName'] : undefined;
          if (shopId && typeof shopId === 'number') {
            user.shopId = shopId;
          }
          if (shopName) {
            user.shopName = shopName;
          }
          if (user.shopId || user.shopName) {
            if (isPlatformBrowser(this.platformId)) {
              localStorage.setItem(AuthStorageKeys.CURRENT_USER, JSON.stringify(user));
            }
            this._currentUser.set({ ...user });
          }
        },
        error: () => {
          // All retries exhausted or non-retryable error — leave shopId undefined, do NOT logout
        },
      });
  }
}
