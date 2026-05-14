package vn.chiendt.haimuoi3.wishlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.chiendt.haimuoi3.product.model.ProductBuyerAvailability;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
    private Long id;
    private String productId;
    private String name;
    private String imageUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductBuyerAvailability availability;
    private int activeSkuCount;
    private LocalDateTime addedAt;
}
