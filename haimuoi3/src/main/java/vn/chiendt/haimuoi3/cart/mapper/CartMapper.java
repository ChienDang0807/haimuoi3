package vn.chiendt.haimuoi3.cart.mapper;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.cart.dto.response.CartItemResponse;
import vn.chiendt.haimuoi3.cart.dto.response.CartResponse;
import vn.chiendt.haimuoi3.cart.model.mongo.CartEntity;
import vn.chiendt.haimuoi3.cart.model.mongo.CartItemEntity;

import java.util.Collections;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toCartResponse(CartEntity entity) {
        List<CartItemResponse> itemResponses = entity.getItems() == null
                ? Collections.emptyList()
                : entity.getItems().stream().map(this::toItemResponse).toList();

        int totalQty = itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum();
        return CartResponse.builder()
                .cartId(entity.getId())
                .userId(entity.getUserId())
                .cartToken(entity.getCartToken())
                .state(entity.getState() != null ? entity.getState().name() : null)
                .items(itemResponses)
                .totalItems(totalQty)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CartItemResponse toItemResponse(CartItemEntity item) {
        return CartItemResponse.builder()
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPriceSnapshot(item.getUnitPriceSnapshot())
                .shopId(item.getShopId())
                .productNameSnapshot(item.getProductNameSnapshot())
                .build();
    }
}
