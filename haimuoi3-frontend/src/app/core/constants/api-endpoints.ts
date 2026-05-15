/**
 * API endpoint paths - chỉ chứa path, không bao gồm base URL
 * Base URL lấy từ environment.apiUrl (vd. http://localhost:8090/api)
 * Convention: /v1/{plural-resource}/...
 */
export const ApiEndpoints = {
  PRODUCTS: '/v1/products',
  PRODUCTS_GLOBAL: '/v1/products/global',
  PRODUCTS_SUGGEST: '/v1/products/suggest',
  PRODUCTS_CART_BATCH: '/v1/products/cart/batch',
  /** GET .../v1/products/:productId/reviews */
  PRODUCTS_REVIEWS: '/reviews',
  /** GET .../v1/products/:productId/reviews/stats */
  PRODUCTS_REVIEWS_STATS: '/reviews/stats',
  /** GET — path param numeric shop PK (Postgres) */
  SHOP_BY_ID: '/v1/shops/by-id',
  /** GET — public shop by slug: `${SHOPS}/${slug}` */
  SHOPS: '/v1/shops',
  /**
   * Shop owner (JWT): GET danh sách đơn; PATCH `.../orders/:id/status` body `{ status }`.
   * Unified transitions: CONFIRMED->READY_TO_SHIP, PAID->READY_TO_SHIP, READY_TO_SHIP->SHIPPING.
   */
  SHOPS_MY_SHOP: '/v1/shops/my-shop',
  SHOPS_MY_SHOP_ORDERS: '/v1/shops/my-shop/orders',
  /** Shop owner inventory (JWT SHOP_OWNER): GET list, PATCH adjust stock */
  SHOPS_MY_SHOP_INVENTORY: '/v1/shops/my-shop/inventory',
  /** Shop owner dashboard aggregate (JWT SHOP_OWNER); query `period=7d` in phase 1 */
  SHOPS_MY_SHOP_DASHBOARD: '/v1/shops/my-shop/dashboard',
  GLOBAL_CATEGORIES: '/v1/global-categories',
  /** Shop owner (JWT SHOP_OWNER): catalog CRUD helpers */
  SHOPS_MY_SHOP_PRODUCTS: '/v1/shops/my-shop/products',
  SHOPS_MY_SHOP_CATEGORIES: '/v1/shops/my-shop/categories',
  /** Guest / anonymous session cart (token header) */
  CART_SESSION: '/v1/carts/session',
  CART_SESSION_ITEMS: '/v1/carts/session/items',
  CART_MERGE: '/v1/carts/merge',
  /** Authenticated customer cart (JWT) */
  CART_ME: '/v1/carts/me',
  CART_ME_ITEMS: '/v1/carts/me/items',
  ORDERS: '/v1/orders',
  ORDERS_CHECKOUT: '/v1/orders/checkout',
  /**
   * Đơn của khách đang đăng nhập (JWT).
   * GET `${CUSTOMERS_ME_ORDERS}` — danh sách (Page); GET `${CUSTOMERS_ME_ORDERS}/:id` — chi tiết (ownership).
   * PATCH `${CUSTOMERS_ME_ORDERS}/:id/cancel` | `.../confirm-delivered` — khách (ownership + rule transition).
   */
  CUSTOMERS_ME_ORDERS: '/v1/customers/me/orders',
  /** Authenticated customer profile (JWT CUSTOMER) */
  CUSTOMERS_ME_PROFILE: '/v1/customers/me/profile',
  CUSTOMERS_ME_CHANGE_PASSWORD: '/v1/customers/me/change-password',
  CUSTOMERS_ME_ADDRESSES: '/v1/customers/me/addresses',
  /** Wishlist (JWT CUSTOMER): GET recent, GET page, POST contains, POST|DELETE by productId */
  CUSTOMERS_ME_WISHLIST: '/v1/customers/me/wishlist',
  PAYMENTS_STRIPE_SESSION: '/v1/payments/stripe/checkout-session',
  AUTH_LOGIN: '/v1/auth/login',
  AUTH_REGISTER: '/v1/auth/register',
  AUTH_ME: '/v1/auth/me',
  AUTH_LOGOUT: '/v1/auth/logout',
  NOTIFICATIONS_ME: '/v1/notifications/me',
  NOTIFICATIONS_READ_ALL: '/v1/notifications/read-all',
} as const;
