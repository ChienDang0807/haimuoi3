export interface Shop {
  id: string;
  /** Slug URL (public shop page) khi có */
  slug?: string;
  name: string;
  description?: string;
  banner?: string;
  avatar?: string;
  rating: number;
  totalRatings?: number;
  memberSince: string;
  totalSales?: number;
}

/** DTO — GET /api/v1/shops/by-id/{id} hoặc GET /api/v1/shops/{slug} (`ShopResponse` backend) */
export interface ShopResponseDto {
  id: number;
  ownerId?: number;
  shopName: string;
  slug?: string;
  description?: string | null;
  logoUrl?: string | null;
  bannerUrl?: string | null;
  email?: string | null;
  phone?: string | null;
  province?: string | null;
  district?: string | null;
  addressDetail?: string | null;
  status?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

/** DTO — GET /api/v1/global-categories (page content) */
export interface GlobalCategoryDto {
  globalCategoryId: string;
  name: string;
  slug: string;
  imageUrl?: string | null;
  displayOrder?: number | null;
  /** Jackson thường serialize field Java `isActive` thành JSON key `active` */
  active?: boolean;
  isActive?: boolean;
}

/** DTO — shop_categories cho shop owner */
export interface ShopCategoryDto {
  shopCategoryId: string;
  shopId: string;
  globalCategoryId: string | null;
  /** Tên global category khi có liên kết (BE enrich) */
  globalCategoryName?: string | null;
  name: string;
  slug: string;
  imageUrl?: string | null;
  displayOrder: number;
  active: boolean;
}

/** Body POST /api/v1/shops/my-shop/categories */
export interface CreateShopCategoryPayload {
  name: string;
  slug: string;
  displayOrder?: number | null;
  imageUrl?: string | null;
  isActive?: boolean | null;
  globalCategoryId?: string | null;
}

/** Body POST /api/v1/shops/my-shop/products (shopId do BE gán) */
export interface CreateShopProductPayload {
  name: string;
  description?: string;
  price: number;
  brand?: string;
  /** Danh mục shop (bắt buộc khi tạo sản phẩm) */
  shopCategoryId: string;
  categoryPublicId?: string;
  featured?: boolean;
  badgeType?: string | null;
  pictures?: { url: string; mimeType?: string }[];
  attributes?: { name: string; values: string[] }[];
  productKind?: 'LEGACY' | 'PARENT' | 'SKU';
  parentProductId?: string;
  sku?: string;
  variantOptions?: Record<string, string>;
}
