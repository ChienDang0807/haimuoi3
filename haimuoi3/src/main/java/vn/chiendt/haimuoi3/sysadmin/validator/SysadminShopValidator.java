package vn.chiendt.haimuoi3.sysadmin.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.sysadmin.dto.request.CreateShopRequest;
import vn.chiendt.haimuoi3.sysadmin.dto.request.UpdateShopRequest;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SysadminShopValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,50}$");

    /**
     * Validate CreateShopRequest
     */
    public void validateCreateShopRequest(CreateShopRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create shop request cannot be null");
        }

        // Validate shop name
        if (request.getShopName() == null || request.getShopName().isBlank()) {
            throw new IllegalArgumentException("Shop name is required");
        }
        if (request.getShopName().length() > Constants.Shop.MAX_SHOP_NAME_LENGTH) {
            throw new IllegalArgumentException("Shop name must not exceed " + Constants.Shop.MAX_SHOP_NAME_LENGTH + " characters");
        }

        // Validate slug
        if (request.getSlug() == null || request.getSlug().isBlank()) {
            throw new IllegalArgumentException("Slug is required");
        }
        if (!SLUG_PATTERN.matcher(request.getSlug()).matches()) {
            throw new IllegalArgumentException("Slug must be 3-50 characters, lowercase, alphanumeric with hyphens only");
        }

        // Validate email (optional but must be valid if provided)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                throw new IllegalArgumentException("Email format is invalid");
            }
        }

        // Validate phone (optional but must be valid if provided)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new IllegalArgumentException("Phone number must be 10-15 digits");
            }
        }

        // Validate description length
        if (request.getDescription() != null && request.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description must not exceed 1000 characters");
        }
    }

    /**
     * Validate UpdateShopRequest
     */
    public void validateUpdateShopRequest(UpdateShopRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update shop request cannot be null");
        }

        // Validate shop name if provided
        if (request.getShopName() != null && !request.getShopName().isBlank()) {
            if (request.getShopName().length() > Constants.Shop.MAX_SHOP_NAME_LENGTH) {
                throw new IllegalArgumentException("Shop name must not exceed " + Constants.Shop.MAX_SHOP_NAME_LENGTH + " characters");
            }
        }

        // Validate email if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                throw new IllegalArgumentException("Email format is invalid");
            }
        }

        // Validate phone if provided
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new IllegalArgumentException("Phone number must be 10-15 digits");
            }
        }

        // Validate description length if provided
        if (request.getDescription() != null && request.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description must not exceed 1000 characters");
        }
    }
}
