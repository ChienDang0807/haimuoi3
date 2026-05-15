package vn.chiendt.haimuoi3.inventory.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.inventory.dto.request.AdjustStockRequest;

@Component
public class AdjustStockRequestValidator {

    public void validate(AdjustStockRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Adjust stock request must not be null");
        }
        validateQuantityOnHand(request.getQuantityOnHand());
    }

    public void validateQuantityOnHand(Integer quantityOnHand) {
        if (quantityOnHand == null) {
            throw new IllegalArgumentException("quantityOnHand is required");
        }
        if (quantityOnHand < 0) {
            throw new IllegalArgumentException("quantityOnHand must be >= 0");
        }
    }
}
