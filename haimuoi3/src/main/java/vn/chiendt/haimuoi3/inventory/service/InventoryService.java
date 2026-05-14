package vn.chiendt.haimuoi3.inventory.service;

import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

public interface InventoryService {

    void assertAvailabilityForCreateOrder(CreateOrderRequest request);

    void applyAfterOrderPlaced(OrderEntity savedOrder);

    void commitHeldForOrder(Long orderId);

    void releaseHeldForOrder(Long orderId);

    /**
     * Khach huy don COD da CONFIRMED: hoan kho theo reservation COMMITTED, hoac theo order_items neu don cu.
     */
    void restockAfterCustomerCancelConfirmedCod(Long orderId, OrderStatus previousStatus, String paymentMethod);

    void ensureProductStockRow(Long shopId, String productId);
}
