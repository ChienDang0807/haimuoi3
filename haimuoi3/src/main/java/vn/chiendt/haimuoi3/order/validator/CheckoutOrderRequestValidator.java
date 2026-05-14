package vn.chiendt.haimuoi3.order.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.order.dto.request.CheckoutOrderRequest;

@Component
public class CheckoutOrderRequestValidator {

    public void validate(CheckoutOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getCartId() == null || request.getCartId().isBlank()) {
            throw new IllegalArgumentException("cartId must not be blank");
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
    }
}
