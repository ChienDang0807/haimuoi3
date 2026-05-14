import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants/api-endpoints';
import {
  ApiResponse,
  PageResponse,
  WishlistContainsRequest,
  WishlistContainsResponse,
  WishlistItemResponse,
} from '../../shared/interfaces';

const baseUrl = `${environment.apiUrl}${ApiEndpoints.CUSTOMERS_ME_WISHLIST}`;

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private readonly http = inject(HttpClient);

  listRecent(limit = 3): Observable<ApiResponse<WishlistItemResponse[]>> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<ApiResponse<WishlistItemResponse[]>>(`${baseUrl}/recent`, { params });
  }

  listPaged(page = 0, size = 12): Observable<ApiResponse<PageResponse<WishlistItemResponse>>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<PageResponse<WishlistItemResponse>>>(baseUrl, { params });
  }

  contains(productIds: string[]): Observable<Record<string, boolean>> {
    const body: WishlistContainsRequest = { productIds };
    return this.http
      .post<ApiResponse<WishlistContainsResponse>>(`${baseUrl}/contains`, body)
      .pipe(map(res => res.result?.contains ?? {}));
  }

  add(productId: string): Observable<WishlistItemResponse> {
    const encoded = encodeURIComponent(productId);
    return this.http
      .post<ApiResponse<WishlistItemResponse>>(`${baseUrl}/${encoded}`, {})
      .pipe(map(res => res.result));
  }

  remove(productId: string): Observable<void> {
    const encoded = encodeURIComponent(productId);
    return this.http.delete(`${baseUrl}/${encoded}`).pipe(map(() => undefined));
  }
}
