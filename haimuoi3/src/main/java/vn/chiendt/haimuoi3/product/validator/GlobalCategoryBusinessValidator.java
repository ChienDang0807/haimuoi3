package vn.chiendt.haimuoi3.product.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateGlobalCategoryRequest;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class GlobalCategoryBusinessValidator {

    private static final Pattern SLUG_PATTERN = Pattern.compile(Constants.Shop.SLUG_REGEX);

    public void validateCreate(CreateGlobalCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create global category request must not be null");
        }
        validateRequiredText(request.getName(), "name");
        validateSlug(request.getSlug());
        validateDisplayOrder(request.getDisplayOrder());
        validateOptionalLength(request.getDescription(), "description", 500);
        validateOptionalLength(request.getSubtitle(), "subtitle", 200);
        validateOptionalLength(request.getCtaText(), "ctaText", 100);
        validateOptionalLength(request.getRoute(), "route", 200);
    }

    public void validateUpdate(UpdateGlobalCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update global category request must not be null");
        }
        if (request.getName() != null && request.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (request.getName() != null) {
            validateOptionalLength(request.getName(), "name", 100);
        }
        validateOptionalLength(request.getDescription(), "description", 500);
        validateOptionalLength(request.getSubtitle(), "subtitle", 200);
        validateOptionalLength(request.getCtaText(), "ctaText", 100);
        validateOptionalLength(request.getRoute(), "route", 200);
        if (request.getDisplayOrder() != null) {
            validateDisplayOrder(request.getDisplayOrder());
        }
    }

    public void validateCategoryId(String categoryId) {
        if (!StringUtils.hasText(categoryId)) {
            throw new IllegalArgumentException("Category ID is required");
        }
    }

    public void validateImageUrl(String imageUrl) {
        validateRequiredText(imageUrl, "imageUrl");
    }

    public void validateImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new IllegalArgumentException("imageUrls must not be empty");
        }
        imageUrls.forEach(this::validateImageUrl);
    }

    private void validateSlug(String slug) {
        validateRequiredText(slug, "slug");
        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new IllegalArgumentException("slug format is invalid");
        }
    }

    private void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private void validateOptionalLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
    }

    private void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder == null) {
            throw new IllegalArgumentException("displayOrder must not be null");
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder must be greater than or equal to 0");
        }
    }
}
