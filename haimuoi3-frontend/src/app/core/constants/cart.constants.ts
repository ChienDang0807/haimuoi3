/**
 * Cart business rules và configuration
 * Reference: haimuoi3/PLAN-Cart-FE.md section 8
 */

/** sessionStorage — Buy now khi chưa đăng nhập; login xong đọc và thêm vào giỏ rồi vào checkout */
export const CartSessionKeys = {
  PENDING_BUY_NOW: 'haimuoi3_pending_buy_now',
} as const;

export const CartRules = {
  /** Số lượng tối thiểu cho mỗi sản phẩm */
  MIN_QUANTITY: 1,
  /** Số lượng tối đa cho mỗi sản phẩm */
  MAX_QUANTITY_PER_ITEM: 999,
  /** Số lượng distinct items tối đa trong cart */
  MAX_DISTINCT_ITEMS: 100,
  /** Thời gian hết hạn của guest cart (days) */
  GUEST_CART_TTL_DAYS: 30,
  /** Cookie name để lưu cart token */
  CART_TOKEN_COOKIE_NAME: 'cart_token',
  /** Header name gửi cart token */
  CART_TOKEN_HEADER_NAME: 'X-Cart-Token',
  /** Debounce time (ms) cho quantity updates */
  DEBOUNCE_UPDATE_MS: 800
} as const;

/**
 * Cart error codes mapping từ backend
 */
export const CartErrorCodes = {
  CART_NOT_FOUND: 1004,
  INVALID_REQUEST: 4000,
  VALIDATION_ERROR: 4001,
  SERVER_ERROR: 5000
} as const;
