package vn.chiendt.haimuoi3.product.model.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.chiendt.haimuoi3.product.model.ProductBadgeType;
import vn.chiendt.haimuoi3.product.model.ProductKind;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "idx_shop_sku_unique", def = "{'shopId': 1, 'sku': 1}", unique = true, sparse = true),
        @CompoundIndex(name = "idx_parent_shop", def = "{'parent_product_id': 1, 'shopId': 1}")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private BigDecimal price;

    @Field("brand")
    private String brand;

    @Field("global_category_id")
    private String globalCategoryId;

    @Field("global_category_name")
    private String globalCategoryName;

    @Field("shop_category_id")
    private String shopCategoryId;

    @Field("shop_category_name")
    private String shopCategoryName;

    @Field("review_count")
    private Double reviewCount;

    @Field("status")
    private String status;

    @Field("featured")
    private boolean featured;

    @Field("attributes")
    private List<ProductAttribute> attributes;

    @Field("pictures")
    private List<ProductPicture> productPictures;

    @Field("shopId")
    private String shopId;

    @Field("badgeType")
    private ProductBadgeType badgeType;

    @Field("product_kind")
    private ProductKind productKind;

    @Field("parent_product_id")
    private String parentProductId;

    @Field("sku")
    private String sku;

    @Field("variant_options")
    private Map<String, String> variantOptions;
}
