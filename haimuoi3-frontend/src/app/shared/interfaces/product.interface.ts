export type ProductKind = 'PARENT' | 'SKU' | 'LEGACY';

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  images?: string[];
  rating?: number;
  reviewCount?: number;
  specifications?: Specification[];
  configurations?: ConfigOption[];
  badge?: ProductBadge;
  shopId?: string;
  /** Slug or id segment cho URL `/category/:slug` */
  category?: string;
  /** Tên hiển thị danh mục (breadcrumb) */
  categoryLabel?: string;
  productKind?: ProductKind;
  parentProductId?: string | null;
  sku?: string | null;
  /** SKU con (chỉ khi productKind === PARENT) */
  skus?: Product[];
  minSkuPrice?: number | null;
  maxSkuPrice?: number | null;
  /** Id gửi lên API giỏ hàng — luôn SKU hoặc LEGACY */
  cartProductId?: string;
}

export interface ProductBadge {
  text: string;
  color: 'black' | 'blue' | 'red' | 'green';
}

export interface Specification {
  label: string;
  value: string;
}

export interface ConfigOption {
  id: string;
  label: string;
  sublabel?: string;
  priceModifier?: number;
  selected?: boolean;
}

export interface NewArrivalsPage {
  products: Product[];
  isLast: boolean;
}

/** Query filters — GET /api/v1/products/global */
export interface GlobalProductsListFilters {
  q?: string | null;
  globalCategoryId?: string | null;
  minPrice?: number | null;
  maxPrice?: number | null;
  minRating?: number | null;
  page?: number;
  size?: number;
}

/** DTO — GET /api/v1/products/global */
export interface BackendGlobalProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string | null;
  badgeType: string | null;
  productKind?: string | null;
  minSkuPrice?: number | string | null;
  maxSkuPrice?: number | string | null;
}

/** DTO — POST /api/v1/products/cart/batch */
export interface BackendCartProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string | null;
  badgeType: string | null;
  productKind?: string | null;
}

/** DTO — GET /api/v1/products/suggest */
export interface ProductSuggestionResponse {
  id: string;
  name: string;
  price: number;
  imageUrl: string | null;
  status: string;
  productKind?: string | null;
}

export interface GetProductsByIdsRequest {
  ids: string[];
}

export interface BackendProductPicture {
  url: string | null;
  mimiType?: string | null;
}

export interface BackendProductAttribute {
  name: string;
  values: string[];
}

/** DTO — GET /api/v1/products/:id (ShopProductResponse) */
export interface ShopProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  brand?: string;
  globalCategoryId?: string | null;
  globalCategoryName?: string | null;
  shopCategoryId?: string | null;
  shopCategoryName?: string | null;
  reviewCount?: number | null;
  status?: string;
  featured?: boolean;
  badgeType?: string | null;
  attributes?: BackendProductAttribute[] | null;
  productPictures?: BackendProductPicture[] | null;
  shopId?: string | null;
  productKind?: string | null;
  parentProductId?: string | null;
  sku?: string | null;
  variantOptions?: Record<string, string> | null;
  skus?: ShopProductResponse[] | null;
  minSkuPrice?: number | string | null;
  maxSkuPrice?: number | string | null;
}

/** DTO — GET /api/v1/products/:productId/reviews (từng phần tử trong `Page.content`) */
export interface ProductReviewResponseDto {
  id: string;
  userName: string;
  userAvatar?: string | null;
  rating: number;
  comment: string;
  date: string;
  verified?: boolean | null;
}

/** DTO — GET /api/v1/products/:productId/reviews/stats */
export interface ProductReviewStatsDto {
  averageRating: number;
  totalReviews: number;
  distribution?: Record<string, number> | null;
}
