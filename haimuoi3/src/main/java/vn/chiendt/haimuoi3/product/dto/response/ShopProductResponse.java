package vn.chiendt.haimuoi3.product.dto.response;

import lombok.*;
import vn.chiendt.haimuoi3.product.model.ProductBadgeType;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductAttribute;
import vn.chiendt.haimuoi3.product.model.mongo.ProductPicture;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private String globalCategoryId;
    private String globalCategoryName;
    private String shopCategoryId;
    private String shopCategoryName;
    private Double reviewCount;
    private String status;
    private boolean featured;
    private ProductBadgeType badgeType;
    private List<ProductAttribute> attributes;
    private List<ProductPicture> productPictures;
    private String shopId;
    private ProductKind productKind;
    private String parentProductId;
    private String sku;
    private Map<String, String> variantOptions;
    /** Chỉ điền khi {@link #productKind} là {@link ProductKind#PARENT}. */
    private List<ShopProductResponse> skus;
    private BigDecimal minSkuPrice;
    private BigDecimal maxSkuPrice;
}
