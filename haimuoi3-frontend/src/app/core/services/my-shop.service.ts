import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants';
import {
  ApiResponse,
  PageResponse,
  ShopProductResponse,
  GlobalCategoryDto,
  ShopCategoryDto,
  CreateShopProductPayload,
  CreateShopCategoryPayload,
} from '../../shared/interfaces';

@Injectable({ providedIn: 'root' })
export class MyShopService {
  private http = inject(HttpClient);
  private readonly root = environment.apiUrl;

  listMyProducts(page = 0, size = 20): Observable<ApiResponse<PageResponse<ShopProductResponse>>> {
    return this.http.get<ApiResponse<PageResponse<ShopProductResponse>>>(
      `${this.root}${ApiEndpoints.SHOPS_MY_SHOP_PRODUCTS}?page=${page}&size=${size}`,
    );
  }

  createMyProduct(body: CreateShopProductPayload): Observable<ApiResponse<ShopProductResponse>> {
    return this.http.post<ApiResponse<ShopProductResponse>>(
      `${this.root}${ApiEndpoints.SHOPS_MY_SHOP_PRODUCTS}`,
      body,
    );
  }

  listMyShopCategories(page = 0, size = 50): Observable<ApiResponse<PageResponse<ShopCategoryDto>>> {
    return this.http.get<ApiResponse<PageResponse<ShopCategoryDto>>>(
      `${this.root}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}?page=${page}&size=${size}`,
    );
  }

  createShopCategory(body: CreateShopCategoryPayload): Observable<ApiResponse<ShopCategoryDto>> {
    return this.http.post<ApiResponse<ShopCategoryDto>>(
      `${this.root}${ApiEndpoints.SHOPS_MY_SHOP_CATEGORIES}`,
      body,
    );
  }

  getGlobalCategories(page = 0, size = 100): Observable<ApiResponse<PageResponse<GlobalCategoryDto>>> {
    return this.http.get<ApiResponse<PageResponse<GlobalCategoryDto>>>(
      `${this.root}${ApiEndpoints.GLOBAL_CATEGORIES}?page=${page}&size=${size}`,
    );
  }
}
