import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, map } from 'rxjs';
import {
  ApiResponse,
  PageResponse,
  Product,
  ProductKind,
  ConfigOption,
  NewArrivalsPage,
  GlobalProductsListFilters,
  BackendGlobalProductResponse,
  BackendCartProductResponse,
  GetProductsByIdsRequest,
  ProductSuggestionResponse,
  ShopProductResponse,
} from '../../shared/interfaces';
import { environment } from '../../../environments/environment';
import { ApiEndpoints, resolveBadgeConfig, ProductDefaults } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}${ApiEndpoints.PRODUCTS}`;
  private readonly fallbackImageUrl = ProductDefaults.FALLBACK_IMAGE_URL;

  getNewArrivalsPage(page = 0, size = 32): Observable<NewArrivalsPage> {
    return this.getGlobalProductsPage({ page, size });
  }

  getGlobalProductsPage(filters: GlobalProductsListFilters): Observable<NewArrivalsPage> {
    const page = filters.page ?? 0;
    const size = filters.size ?? 32;
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    const q = filters.q?.trim();
    if (q) {
      params = params.set('q', q);
    }
    const globalCategoryId = filters.globalCategoryId?.trim();
    if (globalCategoryId) {
      params = params.set('globalCategoryId', globalCategoryId);
    }
    if (filters.minPrice != null) {
      params = params.set('minPrice', String(filters.minPrice));
    }
    if (filters.maxPrice != null) {
      params = params.set('maxPrice', String(filters.maxPrice));
    }
    if (filters.minRating != null) {
      params = params.set('minRating', String(filters.minRating));
    }
    return this.http
      .get<ApiResponse<PageResponse<BackendGlobalProductResponse>>>(`${this.apiUrl}/global`, { params })
      .pipe(
        map(response => ({
          products: response.result.content.map(product => this.mapBackendProduct(product)),
          isLast: response.result.last,
        })),
      );
  }

  getNewArrivals(): Observable<Product[]> {
    const products: Product[] = [
      {
        id: '1',
        name: 'Graphite Slate Tablet Pro',
        description: 'High performance computing',
        price: 1299.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuA0roZ3N44eRlcBM-AvyS5-xSaVaLv-NQWGvc7IIRT9mvh6a5kMmuAXJQUSWqKAKInPR6pfjmWd01FotBnd574JkaieMGvFVwf63QAFOUcSm2jXggZVG97qUv7gYd5SWW3OejnQuYJmwONZ7ALwU4evt1uie0FZhYjBo1YNp7xfC5rT4rf3UTivxbSc-cCG8RXVqejgkHioUcipX72KfZaRF5c_ZeePD0GBYO6p3ivr8BuyFywa3pllTEFIM1YbRLbEBm6ILz3FSG8',
        badge: { text: 'New', color: 'black' }
      },
      {
        id: '2',
        name: 'Brutalism: The Archetype',
        description: 'Premium Grade Paper',
        price: 65.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDzbekriKrRfWFsP12IaHsWAVhXLnYUGixewXUNfB9tQbzymrlUR5r54z1D6JVq84tP9G1FkytB-hsXNXT9BivLSGPyOm0bXBtY082Nv6Liexc90TWRom69oO6LpIEND0TdsNPsvnUiodIdVqSqhl3cXj-c7WiWFwPajiLVoCs19g2xjxpT9MfilAwwLCTOU93NJGpyOqzxRY6AisS-DCrMaS7FjmJvLc6N8SO1JQrJEqnN8q51087lioeX24VHjQXGdmRzsmv_jw0',
        badge: { text: 'Rare', color: 'blue' }
      },
      {
        id: '3',
        name: 'Titanium Electric Kettle',
        description: 'Precision Temperature',
        price: 219.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAaziIV3pz-pVHwlqIXKuS83yqvti6Ekv7P5dluKMbDVpHef06lAWhMGLTS28FpvnTo_togx9xyTIlHo1lbwuTROCtTNSFeDdVZM6q2_AA2NP_fiSwa0V9CDqsctVONftiXUh1wthYq93O_v_RXDzdSyyugY94i-8pUbXXD30a--z-yByJw6iyYF5gSe9n6Osld-GPKuNXGUsWGdRy89Ipn-FfEYqNNyhZlclhUy8V5GPf_QTbQ5o9XGhoocfiowkv2nOwmpQsY92o'
      },
      {
        id: '4',
        name: 'Acoustic Precision X1',
        description: 'Studio Quality',
        price: 450.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBBSe9PAmmMQZHStoTZGZyqVMFj9vdMch1Na96fqSy2zctgyYbfaPv6-KopDFcv7SkdInzwfEU-TpP10DmAuqyJRK2idfZZl8XsHWUNdYWGrw_1G5RuwRYzjcfheBqi_p4R2mp7jc-lin3JTV8o1uAMK-44pYzxHtUGlDnyr7PG_-pXlB8BedBH_yDRgvE8sBd3DjBT2xppSl7m-Rdn5y_4qxL5g4F0umK9WeWwYQEVs14LQbnSJKqFMTmFVp47sLSTJ5oysvO8tOY',
        badge: { text: 'Sale', color: 'red' }
      },
      {
        id: '5',
        name: 'Graphite Slate Tablet Pro',
        description: 'High performance computing',
        price: 1299.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuA0roZ3N44eRlcBM-AvyS5-xSaVaLv-NQWGvc7IIRT9mvh6a5kMmuAXJQUSWqKAKInPR6pfjmWd01FotBnd574JkaieMGvFVwf63QAFOUcSm2jXggZVG97qUv7gYd5SWW3OejnQuYJmwONZ7ALwU4evt1uie0FZhYjBo1YNp7xfC5rT4rf3UTivxbSc-cCG8RXVqejgkHioUcipX72KfZaRF5c_ZeePD0GBYO6p3ivr8BuyFywa3pllTEFIM1YbRLbEBm6ILz3FSG8',
        badge: { text: 'New', color: 'black' }
      },
      {
        id: '6',
        name: 'Brutalism: The Archetype',
        description: 'Premium Grade Paper',
        price: 65.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDzbekriKrRfWFsP12IaHsWAVhXLnYUGixewXUNfB9tQbzymrlUR5r54z1D6JVq84tP9G1FkytB-hsXNXT9BivLSGPyOm0bXBtY082Nv6Liexc90TWRom69oO6LpIEND0TdsNPsvnUiodIdVqSqhl3cXj-c7WiWFwPajiLVoCs19g2xjxpT9MfilAwwLCTOU93NJGpyOqzxRY6AisS-DCrMaS7FjmJvLc6N8SO1JQrJEqnN8q51087lioeX24VHjQXGdmRzsmv_jw0',
        badge: { text: 'Rare', color: 'blue' }
      },
      {
        id: '7',
        name: 'Titanium Electric Kettle',
        description: 'Precision Temperature',
        price: 219.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAaziIV3pz-pVHwlqIXKuS83yqvti6Ekv7P5dluKMbDVpHef06lAWhMGLTS28FpvnTo_togx9xyTIlHo1lbwuTROCtTNSFeDdVZM6q2_AA2NP_fiSwa0V9CDqsctVONftiXUh1wthYq93O_v_RXDzdSyyugY94i-8pUbXXD30a--z-yByJw6iyYF5gSe9n6Osld-GPKuNXGUsWGdRy89Ipn-FfEYqNNyhZlclhUy8V5GPf_QTbQ5o9XGhoocfiowkv2nOwmpQsY92o'
      },
      {
        id: '8',
        name: 'Acoustic Precision X1',
        description: 'Studio Quality',
        price: 450.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBBSe9PAmmMQZHStoTZGZyqVMFj9vdMch1Na96fqSy2zctgyYbfaPv6-KopDFcv7SkdInzwfEU-TpP10DmAuqyJRK2idfZZl8XsHWUNdYWGrw_1G5RuwRYzjcfheBqi_p4R2mp7jc-lin3JTV8o1uAMK-44pYzxHtUGlDnyr7PG_-pXlB8BedBH_yDRgvE8sBd3DjBT2xppSl7m-Rdn5y_4qxL5g4F0umK9WeWwYQEVs14LQbnSJKqFMTmFVp47sLSTJ5oysvO8tOY',
        badge: { text: 'Sale', color: 'red' }
      },
      {
        id: '9',
        name: 'Graphite Slate Tablet Pro',
        description: 'High performance computing',
        price: 1299.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuA0roZ3N44eRlcBM-AvyS5-xSaVaLv-NQWGvc7IIRT9mvh6a5kMmuAXJQUSWqKAKInPR6pfjmWd01FotBnd574JkaieMGvFVwf63QAFOUcSm2jXggZVG97qUv7gYd5SWW3OejnQuYJmwONZ7ALwU4evt1uie0FZhYjBo1YNp7xfC5rT4rf3UTivxbSc-cCG8RXVqejgkHioUcipX72KfZaRF5c_ZeePD0GBYO6p3ivr8BuyFywa3pllTEFIM1YbRLbEBm6ILz3FSG8',
        badge: { text: 'New', color: 'black' }
      },
      {
        id: '10',
        name: 'Brutalism: The Archetype',
        description: 'Premium Grade Paper',
        price: 65.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDzbekriKrRfWFsP12IaHsWAVhXLnYUGixewXUNfB9tQbzymrlUR5r54z1D6JVq84tP9G1FkytB-hsXNXT9BivLSGPyOm0bXBtY082Nv6Liexc90TWRom69oO6LpIEND0TdsNPsvnUiodIdVqSqhl3cXj-c7WiWFwPajiLVoCs19g2xjxpT9MfilAwwLCTOU93NJGpyOqzxRY6AisS-DCrMaS7FjmJvLc6N8SO1JQrJEqnN8q51087lioeX24VHjQXGdmRzsmv_jw0',
        badge: { text: 'Rare', color: 'blue' }
      },
      {
        id: '11',
        name: 'Titanium Electric Kettle',
        description: 'Precision Temperature',
        price: 219.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAaziIV3pz-pVHwlqIXKuS83yqvti6Ekv7P5dluKMbDVpHef06lAWhMGLTS28FpvnTo_togx9xyTIlHo1lbwuTROCtTNSFeDdVZM6q2_AA2NP_fiSwa0V9CDqsctVONftiXUh1wthYq93O_v_RXDzdSyyugY94i-8pUbXXD30a--z-yByJw6iyYF5gSe9n6Osld-GPKuNXGUsWGdRy89Ipn-FfEYqNNyhZlclhUy8V5GPf_QTbQ5o9XGhoocfiowkv2nOwmpQsY92o'
      },
      {
        id: '12',
        name: 'Acoustic Precision X1',
        description: 'Studio Quality',
        price: 450.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBBSe9PAmmMQZHStoTZGZyqVMFj9vdMch1Na96fqSy2zctgyYbfaPv6-KopDFcv7SkdInzwfEU-TpP10DmAuqyJRK2idfZZl8XsHWUNdYWGrw_1G5RuwRYzjcfheBqi_p4R2mp7jc-lin3JTV8o1uAMK-44pYzxHtUGlDnyr7PG_-pXlB8BedBH_yDRgvE8sBd3DjBT2xppSl7m-Rdn5y_4qxL5g4F0umK9WeWwYQEVs14LQbnSJKqFMTmFVp47sLSTJ5oysvO8tOY',
        badge: { text: 'Sale', color: 'red' }
      },
      {
        id: '13',
        name: 'Graphite Slate Tablet Pro',
        description: 'High performance computing',
        price: 1299.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuA0roZ3N44eRlcBM-AvyS5-xSaVaLv-NQWGvc7IIRT9mvh6a5kMmuAXJQUSWqKAKInPR6pfjmWd01FotBnd574JkaieMGvFVwf63QAFOUcSm2jXggZVG97qUv7gYd5SWW3OejnQuYJmwONZ7ALwU4evt1uie0FZhYjBo1YNp7xfC5rT4rf3UTivxbSc-cCG8RXVqejgkHioUcipX72KfZaRF5c_ZeePD0GBYO6p3ivr8BuyFywa3pllTEFIM1YbRLbEBm6ILz3FSG8',
        badge: { text: 'New', color: 'black' }
      },
      {
        id: '14',
        name: 'Brutalism: The Archetype',
        description: 'Premium Grade Paper',
        price: 65.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDzbekriKrRfWFsP12IaHsWAVhXLnYUGixewXUNfB9tQbzymrlUR5r54z1D6JVq84tP9G1FkytB-hsXNXT9BivLSGPyOm0bXBtY082Nv6Liexc90TWRom69oO6LpIEND0TdsNPsvnUiodIdVqSqhl3cXj-c7WiWFwPajiLVoCs19g2xjxpT9MfilAwwLCTOU93NJGpyOqzxRY6AisS-DCrMaS7FjmJvLc6N8SO1JQrJEqnN8q51087lioeX24VHjQXGdmRzsmv_jw0',
        badge: { text: 'Rare', color: 'blue' }
      },
      {
        id: '15',
        name: 'Titanium Electric Kettle',
        description: 'Precision Temperature',
        price: 219.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAaziIV3pz-pVHwlqIXKuS83yqvti6Ekv7P5dluKMbDVpHef06lAWhMGLTS28FpvnTo_togx9xyTIlHo1lbwuTROCtTNSFeDdVZM6q2_AA2NP_fiSwa0V9CDqsctVONftiXUh1wthYq93O_v_RXDzdSyyugY94i-8pUbXXD30a--z-yByJw6iyYF5gSe9n6Osld-GPKuNXGUsWGdRy89Ipn-FfEYqNNyhZlclhUy8V5GPf_QTbQ5o9XGhoocfiowkv2nOwmpQsY92o'
      },
      {
        id: '16',
        name: 'Acoustic Precision X1',
        description: 'Studio Quality',
        price: 450.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBBSe9PAmmMQZHStoTZGZyqVMFj9vdMch1Na96fqSy2zctgyYbfaPv6-KopDFcv7SkdInzwfEU-TpP10DmAuqyJRK2idfZZl8XsHWUNdYWGrw_1G5RuwRYzjcfheBqi_p4R2mp7jc-lin3JTV8o1uAMK-44pYzxHtUGlDnyr7PG_-pXlB8BedBH_yDRgvE8sBd3DjBT2xppSl7m-Rdn5y_4qxL5g4F0umK9WeWwYQEVs14LQbnSJKqFMTmFVp47sLSTJ5oysvO8tOY',
        badge: { text: 'Sale', color: 'red' }
      },
      {
        id: '17',
        name: 'Acoustic Precision X1',
        description: 'Studio Quality',
        price: 450.00,
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBBSe9PAmmMQZHStoTZGZyqVMFj9vdMch1Na96fqSy2zctgyYbfaPv6-KopDFcv7SkdInzwfEU-TpP10DmAuqyJRK2idfZZl8XsHWUNdYWGrw_1G5RuwRYzjcfheBqi_p4R2mp7jc-lin3JTV8o1uAMK-44pYzxHtUGlDnyr7PG_-pXlB8BedBH_yDRgvE8sBd3DjBT2xppSl7m-Rdn5y_4qxL5g4F0umK9WeWwYQEVs14LQbnSJKqFMTmFVp47sLSTJ5oysvO8tOY',
        badge: { text: 'Sale', color: 'red' }
      }
    ];

    return of(products);
  }

  getFeaturedProducts(): Observable<Product[]> {
    return this.getNewArrivals();
  }

  suggestProducts(query: string, limit = 8): Observable<Product[]> {
    const trimmed = query.trim();
    if (!trimmed) {
      return of([]);
    }
    const url = `${environment.apiUrl}${ApiEndpoints.PRODUCTS_SUGGEST}?q=${encodeURIComponent(trimmed)}&limit=${limit}`;
    return this.http
      .get<ApiResponse<ProductSuggestionResponse[]>>(url)
      .pipe(map(response => response.result.map(product => this.mapBackendProduct(product))));
  }

  getProductById(id: string): Observable<Product> {
    return this.http
      .get<ApiResponse<ShopProductResponse>>(`${this.apiUrl}/${encodeURIComponent(id)}`)
      .pipe(
        map(res => {
          if (res.result == null) {
            throw new Error('Empty product response');
          }
          return this.mapShopProductResponse(res.result);
        })
      );
  }

  private mapShopProductResponse(p: ShopProductResponse, opts?: { nestedSku?: boolean }): Product {
    const nestedSku = opts?.nestedSku ?? false;
    const urls = (p.productPictures ?? [])
      .map(x => x.url?.trim())
      .filter((u): u is string => !!u);
    const imageUrl = urls[0] ?? this.fallbackImageUrl;
    const specifications = (p.attributes ?? [])
      .map(attr => ({
        label: (attr.name ?? '').trim(),
        value: (attr.values ?? []).map(v => (v ?? '').trim()).filter(Boolean).join(', '),
      }))
      .filter(s => s.label.length > 0 && s.value.length > 0);

    const categorySlug = p.globalCategoryId?.trim() || undefined;
    const categoryLabel = p.globalCategoryName?.trim() || undefined;
    const priceNum = typeof p.price === 'number' ? p.price : Number(p.price);

    const kind = this.parseProductKind(p.productKind) ?? 'LEGACY';
    const childSkus =
      !nestedSku && (p.skus?.length ?? 0) > 0
        ? (p.skus ?? []).map(s => this.mapShopProductResponse(s, { nestedSku: true }))
        : undefined;

    const configurations: ConfigOption[] | undefined =
      kind === 'PARENT' && childSkus && childSkus.length > 0
        ? childSkus.map((s, i) => ({
            id: s.id,
            label: (s.sku || s.name).trim(),
            sublabel: s.name && s.sku && s.name !== s.sku ? s.name : undefined,
            selected: i === 0,
          }))
        : undefined;

    let displayPrice = Number.isFinite(priceNum) ? priceNum : 0;
    let cartProductId = p.id;
    if (kind === 'PARENT' && childSkus && childSkus.length > 0) {
      cartProductId = childSkus[0].id;
      displayPrice = childSkus[0].price;
    }

    const minSku = this.toOptionalNumber(p.minSkuPrice);
    const maxSku = this.toOptionalNumber(p.maxSkuPrice);

    return {
      id: p.id,
      name: p.name,
      description: p.description ?? '',
      price: displayPrice,
      imageUrl,
      images: urls.length > 0 ? urls : undefined,
      specifications: specifications.length > 0 ? specifications : undefined,
      badge: resolveBadgeConfig(p.badgeType),
      shopId: p.shopId?.trim() || undefined,
      category: categorySlug,
      categoryLabel,
      reviewCount: p.reviewCount != null ? Math.round(p.reviewCount) : undefined,
      productKind: kind,
      parentProductId: p.parentProductId ?? undefined,
      sku: p.sku ?? undefined,
      skus: childSkus && childSkus.length > 0 ? childSkus : undefined,
      minSkuPrice: minSku,
      maxSkuPrice: maxSku,
      cartProductId,
      configurations,
    };
  }

  private parseProductKind(v: string | null | undefined): ProductKind | undefined {
    if (!v) {
      return undefined;
    }
    const u = v.toUpperCase();
    if (u === 'PARENT' || u === 'SKU' || u === 'LEGACY') {
      return u;
    }
    return undefined;
  }

  private toOptionalNumber(v: number | string | null | undefined): number | null | undefined {
    if (v == null) {
      return undefined;
    }
    const n = typeof v === 'number' ? v : Number(v);
    return Number.isFinite(n) ? n : undefined;
  }

  getProductsByShopId(_shopId: string): Observable<Product[]> {
    return this.getNewArrivals();
  }

  getProductsByIds(ids: string[]): Observable<Product[]> {
    const dedupedIds = Array.from(new Set(ids.filter(id => !!id)));
    if (dedupedIds.length === 0) {
      return of([]);
    }

    const request: GetProductsByIdsRequest = { ids: dedupedIds };
    return this.http
      .post<ApiResponse<BackendCartProductResponse[]>>(
        `${environment.apiUrl}${ApiEndpoints.PRODUCTS_CART_BATCH}`,
        request
      )
      .pipe(
        map(response => response.result.map(product => this.mapBackendProduct(product)))
      );
  }

  private mapBackendProduct(
    backendProduct: BackendGlobalProductResponse | BackendCartProductResponse | ProductSuggestionResponse
  ): Product {
    const badgeConfig = 'badgeType' in backendProduct ? resolveBadgeConfig(backendProduct.badgeType) : undefined;
    const imageUrl = backendProduct.imageUrl?.trim() || this.fallbackImageUrl;
    const kind = this.parseProductKind(
      'productKind' in backendProduct ? backendProduct.productKind ?? undefined : undefined
    );
    let price = backendProduct.price;
    if (kind === 'PARENT' && 'minSkuPrice' in backendProduct && backendProduct.minSkuPrice != null) {
      const min = this.toOptionalNumber(backendProduct.minSkuPrice as number | string);
      if (min != null) {
        price = min;
      }
    }
    return {
      id: backendProduct.id,
      name: backendProduct.name,
      description: 'description' in backendProduct ? backendProduct.description : '',
      price,
      imageUrl,
      badge: badgeConfig,
      productKind: kind,
      minSkuPrice:
        'minSkuPrice' in backendProduct
          ? this.toOptionalNumber(backendProduct.minSkuPrice as number | string)
          : undefined,
      maxSkuPrice:
        'maxSkuPrice' in backendProduct
          ? this.toOptionalNumber(backendProduct.maxSkuPrice as number | string)
          : undefined,
      cartProductId: backendProduct.id,
    };
  }
}
