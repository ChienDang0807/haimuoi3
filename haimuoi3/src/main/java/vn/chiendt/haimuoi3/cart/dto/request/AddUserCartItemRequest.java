package vn.chiendt.haimuoi3.cart.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddUserCartItemRequest {

    String productId;

    Integer quantity;

    BigDecimal unitPriceSnapshot;
}
