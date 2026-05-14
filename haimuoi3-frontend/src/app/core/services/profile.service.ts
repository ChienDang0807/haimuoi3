import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants/api-endpoints';
import {
  ApiResponse,
  ChangePasswordRequest,
  CustomerProfile,
  UpdateProfileRequest,
} from '../../shared/interfaces';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly profileUrl = `${environment.apiUrl}${ApiEndpoints.CUSTOMERS_ME_PROFILE}`;
  private readonly changePasswordUrl = `${environment.apiUrl}${ApiEndpoints.CUSTOMERS_ME_CHANGE_PASSWORD}`;

  private readonly _currentProfile = signal<CustomerProfile | null>(null);
  readonly currentProfile = this._currentProfile.asReadonly();

  getProfile(): Observable<ApiResponse<CustomerProfile>> {
    return this.http
      .get<ApiResponse<CustomerProfile>>(this.profileUrl)
      .pipe(tap(res => this._currentProfile.set(res.result ?? null)));
  }

  updateProfile(body: UpdateProfileRequest): Observable<ApiResponse<CustomerProfile>> {
    return this.http
      .put<ApiResponse<CustomerProfile>>(this.profileUrl, body)
      .pipe(tap(res => this._currentProfile.set(res.result ?? null)));
  }

  changePassword(body: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(this.changePasswordUrl, body);
  }

  clear(): void {
    this._currentProfile.set(null);
  }
}
