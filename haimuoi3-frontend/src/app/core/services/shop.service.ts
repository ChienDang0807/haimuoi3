import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse, Shop, ShopResponseDto, Product } from '../../shared/interfaces';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants';

@Injectable({
  providedIn: 'root',
})
export class ShopService {
  private http = inject(HttpClient);
  private readonly shopByIdUrl = `${environment.apiUrl}${ApiEndpoints.SHOP_BY_ID}`;
  private readonly shopPublicBase = `${environment.apiUrl}${ApiEndpoints.SHOPS}`;

  /**
   * Route `/shop/:id` có thể là Postgres id (số) hoặc slug — thử id trước, sau đó slug.
   */
  getShop(shopKey: string): Observable<Shop | null> {
    const trimmed = shopKey.trim();
    const asNum = Number(trimmed);
    if (Number.isFinite(asNum) && asNum > 0 && String(asNum) === trimmed) {
      return this.getShopByNumericId(asNum);
    }
    return this.getShopBySlug(trimmed);
  }

  /** `shopId` từ sản phẩm Mongo: thường là id shop Postgres dạng chuỗi số. */
  getShopById(shopId: string): Observable<Shop | null> {
    const numericId = Number(shopId);
    if (!Number.isFinite(numericId) || numericId <= 0) {
      return of(null);
    }
    return this.getShopByNumericId(numericId);
  }

  private getShopByNumericId(id: number): Observable<Shop | null> {
    return this.http.get<ApiResponse<ShopResponseDto>>(`${this.shopByIdUrl}/${id}`).pipe(
      map(res => this.mapShopResponse(res.result)),
      catchError(() => of(null)),
    );
  }

  private getShopBySlug(slug: string): Observable<Shop | null> {
    return this.http
      .get<ApiResponse<ShopResponseDto>>(`${this.shopPublicBase}/${encodeURIComponent(slug)}`)
      .pipe(
        map(res => this.mapShopResponse(res.result)),
        catchError(() => of(null)),
      );
  }

  private mapShopResponse(dto: ShopResponseDto): Shop {
    const created = dto.createdAt ? new Date(dto.createdAt) : null;
    const memberSince = created && !Number.isNaN(created.getTime())
      ? String(created.getFullYear())
      : '—';

    return {
      id: String(dto.id),
      slug: dto.slug ?? undefined,
      name: dto.shopName,
      description: dto.description ?? undefined,
      banner: dto.bannerUrl ?? undefined,
      avatar: dto.logoUrl ?? undefined,
      rating: 0,
      memberSince,
      totalSales: undefined,
      totalRatings: 0,
    };
  }

  getShopProducts(_shopId: string): Observable<Product[]> {
    const products: Product[] = [
      {
        id: '1',
        name: 'Graphite Chrono S-1',
        description: 'Brushed Titanium / Matte Black',
        price: 450.0,
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuAfzTFY6P0PIhxMaI1-HYCbU_VwvBTFFynLaaeXZJVPEYliv1pSro75F5hB6dl_EEWp8QDYpjmGAjs9fl-FPsYkwfWDa_KIlgS6DbltBZ1HFza9OgM3EA1dBvcmGx8SpLcmBpmRwto0YjhugnEeDOpMvsam4vFpDfXROjLynczRuRSclRq5il3OEItCQCA1dI0FCyRpKRvDBL0B3kiHHVhhFwvvmlXZO88iZCH44I1KvBJzpr_xGXkOVylHVvA6og5P-qxyt15BCoU',
        badge: { text: 'Best Pick', color: 'black' },
      },
      {
        id: '2',
        name: 'Tech Organizer Pro',
        description: 'Full Grain Leather / Slate',
        price: 120.0,
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuCB7_j3qlm5k0Sn_L6PrBUkBEnyyBfjH5zRpcS0eEYPvlgoeFo4aUJYmSxrHY65Or5KshYsZ9DuWETzJIOlda4SBPeYr_4JlKwC8PyMsMTAnX4XymKBtzHx77IWsvXUv7s5jj-Asnh-ShlZdNRgBGrmAxoNChclw3bezo6tMUoPXLbjkcitDEebfqlUFWFQPt5PLLtMmawQEgBDNOVUTvFjEcVzcf7nnTByGBdJxz6lCFKI9_S3EDiK_Cosqkxc2j4Ci3AA1WbULHQ',
      },
      {
        id: '3',
        name: 'Architect Task Lamp',
        description: 'Anodized Aluminum / Warm LED',
        price: 295.0,
        imageUrl:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuCXb6M9JIaksiBsrx4qt3vifUE9YTlnWqSHcxErYx3vR6OkKPAv6RTvZ3avMH-Tiw0EGZeSJHWuTyKfXUtxr5OZjwaam0_olYVKg__5MWrQzO-CkjUi9mvrXn40ukRdYWYJfBOS9WVuHlG_xqSzFMZHfRKTOJ3MXno5T-cE9rtZpDXbrGDaRXEHVIcL31rje5t7NMPVb53DJ7PaVEACO72DVuSzSJ_R2HwLKZ6urtQk9NF2a7H0eqD_iCjn8fYeOqQUo5ecb9R2a2E',
        badge: { text: 'Limited Edition', color: 'blue' },
      },
    ];

    return of(products);
  }
}
