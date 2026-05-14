package vn.chiendt.haimuoi3.cart.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {

    String productId;

    Integer quantity;

    BigDecimal unitPriceSnapshot;

    String shopId;

    String productNameSnapshot;
}
