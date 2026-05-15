import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants';
import {
  ApiResponse,
  CreateShopCategoryPayload,
  CreateShopProductPayload,
  GlobalCategoryDto,
  InventoryItemDto,
  MediaUploadResponseDto,
  OrderDetail,
  PageResponse,
  ShopCategoryDto,
  ShopOwnerDashboardPeriod,
  ShopOwnerDashboardResponse,
  ShopProductResponse,
  ShopResponseDto,
  UpdateShopPayload,
} from '../../shared/interfaces';

/** Aggregate dashboard payload; shape aligned with shop-owner-admin spec when BE is ready. */

@Injectable({ providedIn: 'root' })
export class ShopOwnerApiService {
  private readonly http = inject(HttpClient);
  private readonly apiRoot = environment.apiUrl;
  private readonly myShopOrdersUrl = `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_ORDERS}`;

  listMyProducts(page = 0, size = 20): Observable<ApiResponse<PageResponse<ShopProductResponse>>> {
    return this.http.get<ApiResponse<PageResponse<ShopProductResponse>>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_PRODUCTS}?page=${page}&size=${size}`,
    );
  }

  createMyProduct(body: CreateShopProductPayload): Observable<ApiResponse<ShopProductResponse>> {
    return this.http.post<ApiResponse<ShopProductResponse>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_PRODUCTS}`,
      body,
    );
  }

  listMyShopCategories(page = 0, size = 50): Observable<ApiResponse<PageResponse<ShopCategoryDto>>> {
    return this.http.get<ApiResponse<PageResponse<ShopCategoryDto>>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}?page=${page}&size=${size}`,
    );
  }

  createShopCategory(body: CreateShopCategoryPayload): Observable<ApiResponse<ShopCategoryDto>> {
    return this.http.post<ApiResponse<ShopCategoryDto>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}`,
      body,
    );
  }

  getGlobalCategories(page = 0, size = 100): Observable<ApiResponse<PageResponse<GlobalCategoryDto>>> {
    return this.http.get<ApiResponse<PageResponse<GlobalCategoryDto>>>(
      `${this.apiRoot}${ApiEndpoints.GLOBAL_CATEGORIES}?page=${page}&size=${size}`,
    );
  }

  listMyShopOrders(page = 0, size = 20): Observable<ApiResponse<PageResponse<OrderDetail>>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'createdAt,desc');
    return this.http.get<ApiResponse<PageResponse<OrderDetail>>>(this.myShopOrdersUrl, { params });
  }

  getMyShopOrderById(id: string | number): Observable<ApiResponse<OrderDetail>> {
    return this.http.get<ApiResponse<OrderDetail>>(`${this.myShopOrdersUrl}/${id}`);
  }

  /** Body `{ status }` — BE: CONFIRMED->READY_TO_SHIP, PAID->READY_TO_SHIP, READY_TO_SHIP->SHIPPING. */
  updateMyShopOrderStatus(orderId: number, status: string): Observable<ApiResponse<OrderDetail>> {
    return this.http.patch<ApiResponse<OrderDetail>>(`${this.myShopOrdersUrl}/${orderId}/status`, { status });
  }

  updateMyProduct(productId: string, body: Partial<CreateShopProductPayload>): Observable<ApiResponse<ShopProductResponse>> {
    return this.http.put<ApiResponse<ShopProductResponse>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_PRODUCTS}/${productId}`,
      body,
    );
  }

  toggleProductStatus(productId: string): Observable<ApiResponse<ShopProductResponse>> {
    return this.http.patch<ApiResponse<ShopProductResponse>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_PRODUCTS}/${productId}/status`,
      {},
    );
  }

  updateShopCategory(shopCategoryId: string, body: Partial<CreateShopCategoryPayload>): Observable<ApiResponse<ShopCategoryDto>> {
    return this.http.put<ApiResponse<ShopCategoryDto>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}/${shopCategoryId}`,
      body,
    );
  }

  toggleShopCategoryActive(shopCategoryId: string): Observable<ApiResponse<ShopCategoryDto>> {
    return this.http.patch<ApiResponse<ShopCategoryDto>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}/${shopCategoryId}/active`,
      {},
    );
  }

  deleteShopCategory(shopCategoryId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}/${shopCategoryId}`,
    );
  }

  getMyShopDashboard(period: ShopOwnerDashboardPeriod = '7d'): Observable<ApiResponse<ShopOwnerDashboardResponse>> {
    const params = new HttpParams().set('period', period);
    return this.http.get<ApiResponse<ShopOwnerDashboardResponse>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_DASHBOARD}`,
      { params },
    );
  }

  /** GET /api/v1/shops/my-shop — load current shop profile */
  getMyShop(): Observable<ApiResponse<ShopResponseDto>> {
    return this.http.get<ApiResponse<ShopResponseDto>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP}`,
    );
  }

  /** PUT /api/v1/shops/my-shop — update shop profile */
  updateMyShop(body: UpdateShopPayload): Observable<ApiResponse<ShopResponseDto>> {
    return this.http.put<ApiResponse<ShopResponseDto>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP}`,
      body,
    );
  }

  /** POST /api/v1/media/upload/SHOP — upload shop logo or banner image */
  uploadShopMedia(file: File): Observable<ApiResponse<MediaUploadResponseDto>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<MediaUploadResponseDto>>(
      `${this.apiRoot}/v1/media/upload/SHOP`,
      formData,
    );
  }

  /** GET /api/v1/shops/my-shop/inventory — paginated inventory list */
  listMyShopInventory(page = 0, size = 20): Observable<ApiResponse<PageResponse<InventoryItemDto>>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<ApiResponse<PageResponse<InventoryItemDto>>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_INVENTORY}`,
      { params },
    );
  }

  /** PATCH /api/v1/shops/my-shop/inventory/{productId} — adjust stock quantity */
  adjustMyShopInventory(productId: string, quantityOnHand: number): Observable<ApiResponse<InventoryItemDto>> {
    return this.http.patch<ApiResponse<InventoryItemDto>>(
      `${this.apiRoot}${ApiEndpoints.SHOPS_MY_SHOP_INVENTORY}/${productId}`,
      { quantityOnHand },
    );
  }
}
