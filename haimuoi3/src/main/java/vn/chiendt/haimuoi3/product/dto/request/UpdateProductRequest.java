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
public class UpdateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private String categoryPublicId;
    private Boolean featured;
    private ProductBadgeType badgeType;
    private List<CreateProductRequest.PictureRequest> pictures;
    private List<CreateProductRequest.AttributeRequest> attributes;
    private ProductKind productKind;
    private String parentProductId;
    private String sku;
    private Map<String, String> variantOptions;
}
