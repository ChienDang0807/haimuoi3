package vn.chiendt.haimuoi3.product.dto.request;

import lombok.*;
import vn.chiendt.haimuoi3.product.model.ProductBadgeType;
import vn.chiendt.haimuoi3.product.model.ProductKind;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private String categoryPublicId;
    private Boolean featured;
    private ProductBadgeType badgeType;
    private List<PictureRequest> pictures;
    private List<AttributeRequest> attributes;
    private String shopId;
    /** Danh mục shop (bắt buộc khi tạo qua shop owner); BE suy ra global từ shop_categories nếu có link. */
    private String shopCategoryId;
    private ProductKind productKind;
    private String parentProductId;
    private String sku;
    private Map<String, String> variantOptions;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static public class PictureRequest {
        private String url;
        private String mimeType;

    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static public class AttributeRequest {
        private String name;
        private List<String> values;

    }
}
