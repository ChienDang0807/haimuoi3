package vn.chiendt.haimuoi3.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.order.dto.request.CheckoutOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.UpdateOrderStatusRequest;
import vn.chiendt.haimuoi3.order.dto.response.CheckoutBatchResponse;
import vn.chiendt.haimuoi3.order.dto.response.OrderResponse;
import vn.chiendt.haimuoi3.order.service.OrderService;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ShopService shopService;

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CheckoutBatchResponse> checkoutFromCart(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody CheckoutOrderRequest request) {
        CheckoutBatchResponse result = orderService.checkoutFromCart(currentUser.getId(), request);
        return ApiResponse.success(result, "Checkout completed");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(currentUser.getId(), request);
        return ApiResponse.success(response, "Order created successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ApiResponse.success(response, "Order retrieved successfully");
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<List<OrderResponse>> getOrdersByShopId(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long shopId) {
        ShopResponse shop = shopService.getShopByOwnerId(currentUser.getId());
        if (!shop.getId().equals(shopId)) {
            throw new IllegalArgumentException("shopId does not belong to the current shop owner");
        }
        List<OrderResponse> orders = orderService.getOrdersByShopId(shopId);
        return ApiResponse.success(orders, "Orders retrieved successfully");
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<OrderResponse>> getOrdersByCustomerId(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long customerId) {
        if (!currentUser.getId().equals(customerId)) {
            throw new IllegalArgumentException("Cannot list orders for another customer");
        }
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        return ApiResponse.success(orders, "Orders retrieved successfully");
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ApiResponse.success(response, "Order status updated successfully");
    }
}
