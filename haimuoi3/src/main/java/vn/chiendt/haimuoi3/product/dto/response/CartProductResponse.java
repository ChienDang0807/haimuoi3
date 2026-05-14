package vn.chiendt.haimuoi3.product.dto.response;

import lombok.*;
import vn.chiendt.haimuoi3.product.model.ProductBadgeType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private ProductBadgeType badgeType;
}
