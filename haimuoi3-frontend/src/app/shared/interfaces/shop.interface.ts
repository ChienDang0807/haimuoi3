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


export type ShopOwnerDashboardPeriod = '7d';

export type ShopDashboardTrendDirection = 'up' | 'down' | 'neutral';

export interface ShopDashboardHeaderDto {
  shopName?: string;
  subtitle?: string;
}

export interface ShopDashboardKpiTrendDto {
  direction?: string;
  value?: string;
  isPositive?: boolean;
  positive?: boolean;
}

export interface ShopDashboardKpiCardDto {
  label?: string;
  value?: string;
  icon?: string;
  trend?: ShopDashboardKpiTrendDto;
}

export interface ShopDashboardRevenuePointDto {
  label?: string;
  revenue?: number;
  orderCount?: number;
}

export interface ShopDashboardRecentOrderDto {
  orderId?: number;
  displayOrderCode?: string;
  customerName?: string;
  customerInitials?: string;
  customerColor?: string;
  itemCount?: number;
  status?: string;
  statusLabel?: string;
  timeAgoLabel?: string;
  totalAmount?: number;
}

export interface ShopDashboardOrderStatusSliceDto {
  statusKey?: string;
  label?: string;
  count?: number;
  colorToken?: string;
}

export interface ShopDashboardTopProductDto {
  productId?: string;
  name?: string;
  imageUrl?: string;
  salesCount?: number;
  trendPercent?: number;
  trendDirection?: string;
}

export interface ShopDashboardLowStockAlertDto {
  productId?: string;
  productName?: string;
  quantityOnHand?: number;
}

export interface ShopDashboardShopHealthDto {
  connectionLabel?: string;
  lastSyncedAt?: string;
}

export interface ShopOwnerDashboardResponse {
  header?: ShopDashboardHeaderDto;
  kpiCards?: ShopDashboardKpiCardDto[];
  revenueTrend?: ShopDashboardRevenuePointDto[];
  revenueTrendPrevious?: ShopDashboardRevenuePointDto[];
  recentOrders?: ShopDashboardRecentOrderDto[];
  orderStatusBreakdown?: ShopDashboardOrderStatusSliceDto[];
  fulfillmentRatePercent?: number;
  topProducts?: ShopDashboardTopProductDto[];
  lowStockAlerts?: ShopDashboardLowStockAlertDto[];
  shopHealth?: ShopDashboardShopHealthDto;
  generatedAt?: string;
} 

/** Body PUT /api/v1/shops/my-shop */
export interface UpdateShopPayload {
  shopName?: string;
  description?: string;
  logoUrl?: string;
  bannerUrl?: string;
  email?: string;
  phone?: string;
  province?: string;
  district?: string;
  addressDetail?: string;
}

/** Response from POST /api/v1/media/upload/{targetType} */
export interface MediaUploadResponseDto {
  fileName?: string;
  originalFileName?: string;
  contentType?: string;
  size?: number;
  url?: string;
}
