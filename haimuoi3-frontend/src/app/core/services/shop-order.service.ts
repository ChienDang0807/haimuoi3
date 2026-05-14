import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants/api-endpoints';
import { ApiResponse, OrderDetail, PageResponse } from '../../shared/interfaces';

const url = `${environment.apiUrl}${ApiEndpoints.SHOPS_MY_SHOP_ORDERS}`;

@Injectable({ providedIn: 'root' })
export class ShopOrderService {
  private readonly http = inject(HttpClient);

  listMyShopOrders(page = 0, size = 20): Observable<ApiResponse<PageResponse<OrderDetail>>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<PageResponse<OrderDetail>>>(url, { params });
  }

  getOrderById(id: string | number): Observable<ApiResponse<OrderDetail>> {
    return this.http.get<ApiResponse<OrderDetail>>(`${url}/${id}`);
  }

  /** Body `{ status }` — BE: CONFIRMED->READY_TO_SHIP, PAID->READY_TO_SHIP, READY_TO_SHIP->SHIPPING. */
  updateMyShopOrderStatus(orderId: number, status: string): Observable<ApiResponse<OrderDetail>> {
    return this.http.patch<ApiResponse<OrderDetail>>(`${url}/${orderId}/status`, { status });
  }
}
