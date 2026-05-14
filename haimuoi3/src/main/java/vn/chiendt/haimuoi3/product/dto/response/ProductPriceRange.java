package vn.chiendt.haimuoi3.product.dto.response;

import java.math.BigDecimal;

/**
 * Khoảng giá SKU con (dùng khi hydrate catalog PARENT).
 */
public record ProductPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
}
