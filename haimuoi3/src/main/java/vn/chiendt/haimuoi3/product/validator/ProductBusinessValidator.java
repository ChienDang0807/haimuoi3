package vn.chiendt.haimuoi3.product.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.product.dto.request.CreateProductRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateProductRequest;
import vn.chiendt.haimuoi3.product.model.ProductKind;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductBusinessValidator {

    private final ProductParentSkuValidator productParentSkuValidator;

    public void validateCreate(CreateProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create product request must not be null");
        }
        validateRequiredText(request.getName(), "name");
        validateRequiredText(request.getShopId(), "shopId");
        validateRequiredText(request.getShopCategoryId(), "shopCategoryId");
        validateCreatePriceByKind(request);
        productParentSkuValidator.validateCreate(request);
    }

    public void validateUpdate(UpdateProductRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update product request must not be null");
        }
        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
        }
    }

    private void validateCreatePriceByKind(CreateProductRequest request) {
        ProductKind kind = request.getProductKind() == null ? ProductKind.LEGACY : request.getProductKind();
        if (kind == ProductKind.PARENT) {
            if (request.getPrice() != null) {
                validatePrice(request.getPrice());
            }
            return;
        }
        validatePrice(request.getPrice());
    }

    public void validateGlobalFilter(String query, BigDecimal minPrice, BigDecimal maxPrice, Double minRating) {
        validateSearchQueryLength(query);
        if (minPrice != null && minPrice.signum() < 0) {
            throw new IllegalArgumentException("minPrice must be greater than or equal to 0");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("maxPrice must be greater than or equal to 0");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }
        if (minRating != null && (minRating < 0 || minRating > 5)) {
            throw new IllegalArgumentException("minRating must be between 0 and 5");
        }
    }

    public int normalizeSuggestLimit(Integer limit) {
        if (limit == null) {
            return Constants.Product.DEFAULT_SUGGEST_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be greater than or equal to 1");
        }
        return Math.min(limit, Constants.Product.MAX_SUGGEST_LIMIT);
    }

    public void validateSuggestQuery(String query) {
        if (query == null || query.isBlank()) {
            return;
        }
        validateSearchQueryLength(query);
    }

    private void validateRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must be greater than or equal to 0");
        }
    }

    private void validateSearchQueryLength(String query) {
        if (query != null && query.trim().length() > Constants.Product.MAX_SEARCH_QUERY_LENGTH) {
            throw new IllegalArgumentException("q must be less than or equal to 50 characters");
        }
    }
}
