package vn.chiendt.haimuoi3.order.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.order.dto.request.CheckoutOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.UpdateOrderStatusRequest;
import vn.chiendt.haimuoi3.order.dto.response.CheckoutBatchResponse;
import vn.chiendt.haimuoi3.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long customerId, CreateOrderRequest request);

    CheckoutBatchResponse checkoutFromCart(Long customerId, CheckoutOrderRequest request);

    OrderResponse getOrderForCustomer(Long customerId, Long orderId);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByShopId(Long shopId);

    Page<OrderResponse> getOrdersByShopId(Long shopId, Pageable pageable);

    OrderResponse getOrderByIdAndShopId(Long orderId, Long shopId);

    Page<OrderResponse> getOrdersForShopOwner(Long ownerUserId, Pageable pageable);

    OrderResponse getOrderForShopOwner(Long ownerUserId, Long orderId);

    List<OrderResponse> getOrdersByCustomerId(Long customerId);

    Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    /**
     * Shop owner: chi transition CONFIRMED→READY_TO_SHIP, PAID→READY_TO_SHIP, READY_TO_SHIP→SHIPPING
     * (xem ShopOrderStatusTransitionValidator).
     */
    OrderResponse updateOrderStatusByShopOwner(Long ownerUserId, Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelMyOrder(Long customerId, Long orderId);

    OrderResponse confirmDeliveredMyOrder(Long customerId, Long orderId);
}
