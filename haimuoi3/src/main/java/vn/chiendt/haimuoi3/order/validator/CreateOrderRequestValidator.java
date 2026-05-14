package vn.chiendt.haimuoi3.order.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.OrderItemRequest;

import java.util.List;

@Component
public class CreateOrderRequestValidator {

    public void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getShopId() == null || request.getShopId() <= 0) {
            throw new IllegalArgumentException("shopId must be positive");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("shippingAddress must not be blank");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new IllegalArgumentException("paymentMethod must not be blank");
        }
        if (request.getTotalAmount() == null || request.getTotalAmount().signum() < 0) {
            throw new IllegalArgumentException("totalAmount must be non-negative");
        }

        List<OrderItemRequest> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        for (OrderItemRequest item : items) {
            validateOrderItem(item);
        }
    }

    public void validatePositiveOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("order id must be positive");
        }
    }

    private void validateOrderItem(OrderItemRequest item) {
        if (item == null) {
            throw new IllegalArgumentException("order item must not be null");
        }
        if (item.getProductId() == null || item.getProductId().isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        if (item.getProductName() == null || item.getProductName().isBlank()) {
            throw new IllegalArgumentException("productName must not be blank");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("item quantity must be greater than 0");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be non-negative");
        }
        if (item.getSubtotal() == null || item.getSubtotal().signum() < 0) {
            throw new IllegalArgumentException("subtotal must be non-negative");
        }
    }
}
