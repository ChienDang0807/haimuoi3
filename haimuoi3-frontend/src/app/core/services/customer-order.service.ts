import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants/api-endpoints';
import { ApiResponse, OrderDetail, PageResponse } from '../../shared/interfaces';

const baseUrl = `${environment.apiUrl}${ApiEndpoints.CUSTOMERS_ME_ORDERS}`;

@Injectable({ providedIn: 'root' })
export class CustomerOrderService {
  private readonly http = inject(HttpClient);

  listMyOrders(page = 0, size = 20): Observable<ApiResponse<PageResponse<OrderDetail>>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<PageResponse<OrderDetail>>>(baseUrl, { params });
  }

  getMyOrder(id: number): Observable<ApiResponse<OrderDetail>> {
    return this.http.get<ApiResponse<OrderDetail>>(`${baseUrl}/${id}`);
  }

  cancelMyOrder(id: number): Observable<ApiResponse<OrderDetail>> {
    return this.http.patch<ApiResponse<OrderDetail>>(`${baseUrl}/${id}/cancel`, {});
  }

  confirmDeliveredMyOrder(id: number): Observable<ApiResponse<OrderDetail>> {
    return this.http.patch<ApiResponse<OrderDetail>>(`${baseUrl}/${id}/confirm-delivered`, {});
  }
}
