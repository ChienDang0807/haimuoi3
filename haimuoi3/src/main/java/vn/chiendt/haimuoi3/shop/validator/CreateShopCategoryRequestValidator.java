package vn.chiendt.haimuoi3.shop.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.shop.dto.request.CreateShopCategoryRequest;

@Component
public class CreateShopCategoryRequestValidator {

    public void validate(CreateShopCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create shop category request must not be null");
        }
        validateRequiredText(request.getName(), "name");
        validateRequiredText(request.getSlug(), "slug");
        if (request.getDisplayOrder() != null && request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("displayOrder must be greater than or equal to 0");
        }
    }

    private static void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
