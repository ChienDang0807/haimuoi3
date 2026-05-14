package vn.chiendt.haimuoi3.product.dto.response;

import lombok.*;
import vn.chiendt.haimuoi3.product.model.ProductBadgeType;
import vn.chiendt.haimuoi3.product.model.ProductKind;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private ProductBadgeType badgeType;
    private ProductKind productKind;
    private BigDecimal minSkuPrice;
    private BigDecimal maxSkuPrice;
}
