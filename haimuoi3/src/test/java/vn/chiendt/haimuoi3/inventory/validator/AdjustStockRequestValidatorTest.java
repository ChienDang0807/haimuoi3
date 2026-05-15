package vn.chiendt.haimuoi3.inventory.validator;

import org.junit.jupiter.api.Test;
import vn.chiendt.haimuoi3.inventory.dto.request.AdjustStockRequest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdjustStockRequestValidatorTest {

    private final AdjustStockRequestValidator validator = new AdjustStockRequestValidator();

    @Test
    void validate_acceptsNonNegativeQuantity() {
        assertThatCode(() -> validator.validate(new AdjustStockRequest(10)))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_rejectsNullRequest() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Adjust stock request must not be null");
    }

    @Test
    void validate_rejectsNullQuantity() {
        assertThatThrownBy(() -> validator.validate(new AdjustStockRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantityOnHand is required");
    }

    @Test
    void validate_rejectsNegativeQuantity() {
        assertThatThrownBy(() -> validator.validate(new AdjustStockRequest(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantityOnHand must be >= 0");
    }
}
