package vn.chiendt.haimuoi3.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.order.dto.response.OrderResponse;
import vn.chiendt.haimuoi3.order.service.OrderService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@RestController
@RequestMapping("/api/v1/customers/me/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<Page<OrderResponse>> listMyOrders(
            @AuthenticationPrincipal UserEntity currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<OrderResponse> page = orderService.getOrdersByCustomerId(currentUser.getId(), pageable);
        return ApiResponse.success(page, "Orders retrieved successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getMyOrder(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        OrderResponse response = orderService.getOrderForCustomer(currentUser.getId(), id);
        return ApiResponse.success(response, "Order retrieved successfully");
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelMyOrder(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        OrderResponse response = orderService.cancelMyOrder(currentUser.getId(), id);
        return ApiResponse.success(response, "Order cancelled successfully");
    }

    @PatchMapping("/{id}/confirm-delivered")
    public ApiResponse<OrderResponse> confirmDeliveredMyOrder(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable Long id) {
        OrderResponse response = orderService.confirmDeliveredMyOrder(currentUser.getId(), id);
        return ApiResponse.success(response, "Order marked as delivered");
    }
}
