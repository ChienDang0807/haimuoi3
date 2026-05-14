package vn.chiendt.haimuoi3.order.validator;

import org.junit.jupiter.api.Test;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopOrderStatusTransitionValidatorTest {

    private final ShopOrderStatusTransitionValidator validator = new ShopOrderStatusTransitionValidator();

    @Test
    void codOrder_allowsConfirmedToReadyToShip() {
        assertThatCode(() -> validator.validateTransition(
                OrderStatus.CONFIRMED,
                OrderStatus.READY_TO_SHIP,
                "COD"
        )).doesNotThrowAnyException();
    }

    @Test
    void codOrder_rejectsPaidToReadyToShip() {
        assertThatThrownBy(() -> validator.validateTransition(
                OrderStatus.PAID,
                OrderStatus.READY_TO_SHIP,
                "COD"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stripeOrder_allowsPaidToReadyToShip() {
        assertThatCode(() -> validator.validateTransition(
                OrderStatus.PAID,
                OrderStatus.READY_TO_SHIP,
                "STRIPE"
        )).doesNotThrowAnyException();
    }

    @Test
    void stripeOrder_rejectsConfirmedToReadyToShip() {
        assertThatThrownBy(() -> validator.validateTransition(
                OrderStatus.CONFIRMED,
                OrderStatus.READY_TO_SHIP,
                "STRIPE"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
