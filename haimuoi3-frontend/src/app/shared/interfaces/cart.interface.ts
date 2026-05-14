/**
 * Cart interfaces theo backend contract
 * Reference: haimuoi3/PLAN-Cart-FE.md
 */

export interface CartItem {
  productId: string;
  quantity: number;
  unitPriceSnapshot: number;
  shopId?: string;
  productNameSnapshot?: string;
}

export interface Cart {
  cartId: string;
  userId: string | null;
  cartToken?: string | null;
  state: string;
  items: CartItem[];
  totalItems: number;
  createdAt: string;
  updatedAt: string;
}

export interface AddCartItemRequest {
  productId: string;
  quantity: number;
  unitPriceSnapshot: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface MergeCartRequest {
  guestCartToken: string;
  userId: string;
}

export interface MergeCartResponse {
  merged: boolean;
  mergedItemsCount: number;
  notifications: string[];
}
