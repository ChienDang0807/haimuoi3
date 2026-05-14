/** Khớp enum backend `ProductBuyerAvailability`. */
export type ProductBuyerAvailability = 'AVAILABLE' | 'OUT_OF_STOCK' | 'DISCONTINUED';

export interface WishlistItemResponse {
  id: number;
  productId: string;
  name: string;
  imageUrl: string | null;
  minPrice: number | string | null;
  maxPrice: number | string | null;
  availability: ProductBuyerAvailability;
  activeSkuCount: number;
  addedAt: string;
}

export interface WishlistContainsRequest {
  productIds: string[];
}

export interface WishlistContainsResponse {
  contains: Record<string, boolean>;
}
