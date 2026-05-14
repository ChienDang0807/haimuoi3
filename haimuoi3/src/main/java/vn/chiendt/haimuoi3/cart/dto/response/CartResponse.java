package vn.chiendt.haimuoi3.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    String cartId;

    String userId;

    String cartToken;

    String state;

    List<CartItemResponse> items;

    int totalItems;

    Instant createdAt;

    Instant updatedAt;
}
