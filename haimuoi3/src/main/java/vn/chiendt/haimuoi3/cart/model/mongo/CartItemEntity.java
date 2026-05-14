package vn.chiendt.haimuoi3.cart.model.mongo;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemEntity {

    String id;

    String productId;

    Integer quantity;

    BigDecimal unitPriceSnapshot;

    /** Chuỗi numeric shop PK (Postgres), đồng bộ với `products.shopId` Mongo. */
    @Field("shop_id")
    String shopId;

    @Field("product_name_snapshot")
    String productNameSnapshot;
}
