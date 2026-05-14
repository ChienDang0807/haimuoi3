package vn.chiendt.haimuoi3.cart.model.mongo;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartEntity {

    @Id
    String id;

    @Field("user_id")
    @Indexed
    String userId;

    @Field("cart_token")
    @Indexed(unique = true, sparse = true)
    String cartToken;

    @Field("state")
    @Builder.Default
    CartState state = CartState.ACTIVE;

    @Field("items")
    @Builder.Default
    List<CartItemEntity> items = new ArrayList<>();

    @Field("expires_at")
    @Indexed
    Instant expiresAt;

    @Field("created_at")
    @Builder.Default
    Instant createdAt = Instant.now();

    @Field("updated_at")
    @Builder.Default
    Instant updatedAt = Instant.now();
}
