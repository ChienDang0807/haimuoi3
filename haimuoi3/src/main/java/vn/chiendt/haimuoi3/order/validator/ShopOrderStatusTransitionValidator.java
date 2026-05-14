package vn.chiendt.haimuoi3.order.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

/**
 * Luong unified (COD + online):
 * {@code CONFIRMED} -> {@code READY_TO_SHIP},
 * {@code PAID} -> {@code READY_TO_SHIP},
 * {@code READY_TO_SHIP} -> {@code SHIPPING}.
 */
@Component
public class ShopOrderStatusTransitionValidator {

    public void validateTransition(OrderStatus current, OrderStatus target, String paymentMethod) {
        if (target == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (current == null) {
            throw new IllegalArgumentException("order has no current status");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("paymentMethod must not be blank");
        }

        String normalizedPaymentMethod = paymentMethod.trim().toUpperCase();
        boolean allowed = switch (current) {
            case CONFIRMED -> "COD".equals(normalizedPaymentMethod) && target == OrderStatus.READY_TO_SHIP;
            case PAID -> "STRIPE".equals(normalizedPaymentMethod) && target == OrderStatus.READY_TO_SHIP;
            case READY_TO_SHIP -> target == OrderStatus.SHIPPING;
            default -> false;
        };
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Shop cannot change order from " + current + " to " + target + " for payment method "
                            + normalizedPaymentMethod
                            + ". Allowed: COD(CONFIRMED→READY_TO_SHIP), STRIPE(PAID→READY_TO_SHIP), READY_TO_SHIP→SHIPPING.");
        }
    }
}
