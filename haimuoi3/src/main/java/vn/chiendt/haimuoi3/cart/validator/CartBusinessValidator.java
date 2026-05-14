package vn.chiendt.haimuoi3.cart.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.cart.dto.request.AddCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.AddUserCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.MergeCartRequest;
import vn.chiendt.haimuoi3.common.constants.Constants;

import java.util.UUID;

@Component
public class CartBusinessValidator {

    public void validateCartToken(String cartToken) {
        if (cartToken == null || cartToken.isBlank()) {
            throw new IllegalArgumentException("cartToken must not be blank");
        }
        try {
            UUID.fromString(cartToken);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("cartToken must be a valid UUID");
        }
    }

    public void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }

    public void validatePositiveUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("user id must be positive");
        }
    }

    public void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
    }

    public void validateAddItemRequest(AddCartItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        validateCartToken(request.getCartToken());
        validateProductId(request.getProductId());
        validateQuantity(request.getQuantity());
    }

    public void validateAddUserCartItemRequest(AddUserCartItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        validateProductId(request.getProductId());
        validateQuantity(request.getQuantity());
    }

    public void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
        if (quantity > Constants.Cart.MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("quantity must not exceed " + Constants.Cart.MAX_QUANTITY_PER_ITEM);
        }
    }

    public void validateMaxDistinctItems(int currentCount) {
        if (currentCount >= Constants.Cart.MAX_DISTINCT_ITEMS) {
            throw new IllegalArgumentException("cart cannot have more than " + Constants.Cart.MAX_DISTINCT_ITEMS + " distinct items");
        }
    }

    public void validateMergeRequest(MergeCartRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("merge request must not be null");
        }
        validateCartToken(request.getGuestCartToken());
        validateUserId(request.getUserId());
    }
}
