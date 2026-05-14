package vn.chiendt.haimuoi3.cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.cart.dto.request.AddCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.AddUserCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.MergeCartRequest;
import vn.chiendt.haimuoi3.cart.dto.request.UpdateCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.request.UpdateUserCartItemRequest;
import vn.chiendt.haimuoi3.cart.dto.response.CartResponse;
import vn.chiendt.haimuoi3.cart.dto.response.MergeCartResponse;
import vn.chiendt.haimuoi3.cart.service.CartService;
import vn.chiendt.haimuoi3.cart.util.CartTokenExtractor;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/session")
    public ApiResponse<CartResponse> createOrGetGuestCart(HttpServletRequest request) {
        String cartToken = CartTokenExtractor.extract(request);
        CartResponse cart = cartService.createOrGetGuestCart(cartToken);
        return ApiResponse.<CartResponse>builder()
                .message("Guest cart ready")
                .result(cart)
                .build();
    }

    @PostMapping("/session/items")
    public ApiResponse<CartResponse> addGuestItem(
            @RequestBody AddCartItemRequest body,
            HttpServletRequest request) {
        String cartToken = CartTokenExtractor.extract(request);
        body.setCartToken(cartToken);
        CartResponse cart = cartService.addGuestItem(body);
        return ApiResponse.<CartResponse>builder()
                .message("Item added to cart")
                .result(cart)
                .build();
    }

    @GetMapping("/session/{cartToken}")
    public ApiResponse<CartResponse> getGuestCart(@PathVariable String cartToken) {
        CartResponse cart = cartService.getGuestCart(cartToken);
        return ApiResponse.<CartResponse>builder()
                .message("Guest cart fetched")
                .result(cart)
                .build();
    }

    @PostMapping("/merge")
    public ApiResponse<MergeCartResponse> mergeGuestCartToUser(@RequestBody MergeCartRequest request) {
        MergeCartResponse result = cartService.mergeGuestCartToUser(request);
        return ApiResponse.<MergeCartResponse>builder()
                .message("Cart merge completed")
                .result(result)
                .build();
    }

    @DeleteMapping("/session/{cartToken}/items/{productId}")
    public ApiResponse<CartResponse> removeGuestItem(
            @PathVariable String cartToken,
            @PathVariable String productId) {
        CartResponse cart = cartService.removeItem(cartToken, productId);
        return ApiResponse.<CartResponse>builder()
                .message("Item removed from cart")
                .result(cart)
                .build();
    }

    @PatchMapping("/session/{cartToken}/items/{productId}")
    public ApiResponse<CartResponse> updateGuestItemQuantity(
            @PathVariable String cartToken,
            @PathVariable String productId,
            @RequestBody UpdateCartItemRequest body) {
        CartResponse cart = cartService.updateItemQuantity(cartToken, productId, body.getQuantity());
        return ApiResponse.<CartResponse>builder()
                .message("Item quantity updated")
                .result(cart)
                .build();
    }

    @DeleteMapping("/session/{cartToken}")
    public ApiResponse<Void> clearGuestCart(@PathVariable String cartToken) {
        cartService.clearCart(cartToken);
        return ApiResponse.<Void>builder()
                .message("Cart cleared")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<CartResponse> getMyCart(@AuthenticationPrincipal UserEntity currentUser) {
        CartResponse cart = cartService.getOrCreateUserCart(currentUser.getId());
        return ApiResponse.success(cart, "Cart retrieved successfully");
    }

    @PostMapping("/me/items")
    public ApiResponse<CartResponse> addMyCartItem(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody AddUserCartItemRequest request) {
        CartResponse cart = cartService.addUserCartItem(currentUser.getId(), request);
        return ApiResponse.success(cart, "Item added to cart");
    }

    @PatchMapping("/me/items/{productId}")
    public ApiResponse<CartResponse> updateMyCartItemQuantity(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable String productId,
            @RequestBody UpdateUserCartItemRequest body) {
        CartResponse cart = cartService.updateUserCartItemQuantity(
                currentUser.getId(), productId, body.getQuantity());
        return ApiResponse.success(cart, "Item quantity updated");
    }

    @DeleteMapping("/me/items/{productId}")
    public ApiResponse<CartResponse> removeMyCartItem(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable String productId) {
        CartResponse cart = cartService.removeUserCartItem(currentUser.getId(), productId);
        return ApiResponse.success(cart, "Item removed from cart");
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> clearMyCart(@AuthenticationPrincipal UserEntity currentUser) {
        cartService.clearUserCart(currentUser.getId());
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Cart cleared")
                .build();
    }
}
