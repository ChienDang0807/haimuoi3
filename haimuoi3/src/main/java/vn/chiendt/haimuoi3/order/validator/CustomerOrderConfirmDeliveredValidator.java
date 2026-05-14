package vn.chiendt.haimuoi3.order.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

@Component
public class CustomerOrderConfirmDeliveredValidator {

    public void validateCurrentStatus(OrderStatus current) {
        if (current != OrderStatus.SHIPPING) {
            throw new IllegalArgumentException("Order can only be confirmed as delivered when status is SHIPPING");
        }
    }
}
