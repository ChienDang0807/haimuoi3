package vn.chiendt.haimuoi3.order.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

@Component
public class CustomerOrderCancelValidator {

    public void validateCurrentStatus(OrderStatus current) {
        if (!Constants.Order.CUSTOMER_CANCELLABLE_STATUSES.contains(current)) {
            throw new IllegalArgumentException("Order cannot be cancelled in status: " + current);
        }
    }
}
