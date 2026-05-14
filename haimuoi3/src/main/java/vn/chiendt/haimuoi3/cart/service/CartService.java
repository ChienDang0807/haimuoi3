package vn.chiendt.haimuoi3.cart.service;

import vn.chiendt.haimuoi3.cart.dto.request.AddCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.AddUserCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.MergeCartRequest;
import vn.chiendt.haimuoi3.cart.dto.response.CartResponse;
import vn.chiendt.haimuoi3.cart.dto.response.MergeCartResponse;

public interface CartService {

    CartResponse createOrGetGuestCart(String cartToken);

    CartResponse addGuestItem(AddCartItemRequest request);

    CartResponse getGuestCart(String cartToken);

    CartResponse getUserCart(String userId);

    CartResponse getOrCreateUserCart(Long userId);

    CartResponse addUserCartItem(Long userId, AddUserCartItemRequest request);

    CartResponse updateUserCartItemQuantity(Long userId, String productId, Integer newQuantity);

    CartResponse removeUserCartItem(Long userId, String productId);

    void clearUserCart(Long userId);

    MergeCartResponse mergeGuestCartToUser(MergeCartRequest request);

    CartResponse removeItem(String cartToken, String productId);

    CartResponse updateItemQuantity(String cartToken, String productId, int newQuantity);

    void clearCart(String cartToken);
}
