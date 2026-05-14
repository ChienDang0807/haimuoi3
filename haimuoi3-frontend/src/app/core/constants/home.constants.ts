/**
 * Constants cho Home page và các section con
 */

/**
 * New Arrivals section configuration
 */
export const NewArrivals = {
  /** Số sản phẩm hiển thị ban đầu */
  INITIAL_VISIBLE_COUNT: 12,
  /** Số sản phẩm tăng thêm mỗi lần "Xem thêm" */
  LOAD_MORE_STEP: 8,
  /** Kích thước page khi gọi API */
  PAGE_SIZE: 32
} as const;

/**
 * Product display defaults
 */
export const ProductDefaults = {
  /** Ảnh fallback khi imageUrl null hoặc rỗng */
  FALLBACK_IMAGE_URL: 'https://via.placeholder.com/600x600?text=No+Image'
} as const;
