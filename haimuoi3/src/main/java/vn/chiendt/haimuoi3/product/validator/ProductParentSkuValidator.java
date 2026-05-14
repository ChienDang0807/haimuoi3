package vn.chiendt.haimuoi3.product.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.dto.request.CreateProductRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateProductRequest;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductParentSkuValidator {

    private final ProductRepository productRepository;

    public void validateCreate(CreateProductRequest request) {
        ProductKind kind = request.getProductKind() == null ? ProductKind.LEGACY : request.getProductKind();
        switch (kind) {
            case LEGACY -> validateLegacyCreate(request);
            case PARENT -> validateParentCreate(request);
            case SKU -> validateSkuCreate(request);
            default -> throw new IllegalArgumentException("Unsupported productKind: " + kind);
        }
    }

    public void validateUpdate(ProductEntity existing, UpdateProductRequest request) {
        ProductKind nextKind = resolveKindAfterUpdate(existing, request);
        if (nextKind == ProductKind.SKU) {
            if (request.getSku() != null && !request.getSku().isBlank()) {
                String shopId = existing.getShopId();
                if (shopId != null && productRepository.existsByShopIdAndSkuAndIdNot(shopId.trim(), request.getSku().trim(), existing.getId())) {
                    throw new IllegalArgumentException("sku already exists for this shop");
                }
            }
        }
        if (nextKind == ProductKind.PARENT && request.getSku() != null && !request.getSku().isBlank()) {
            throw new IllegalArgumentException("PARENT must not have sku");
        }
    }

    private ProductKind resolveKindAfterUpdate(ProductEntity existing, UpdateProductRequest request) {
        ProductKind k = request.getProductKind() != null ? request.getProductKind() : ProductKind.resolve(existing);
        return k;
    }

    private void validateLegacyCreate(CreateProductRequest request) {
        if (request.getParentProductId() != null && !request.getParentProductId().isBlank()) {
            throw new IllegalArgumentException("LEGACY product must not have parentProductId");
        }
        if (request.getSku() != null && !request.getSku().isBlank()) {
            throw new IllegalArgumentException("LEGACY product must not have sku");
        }
        validateRequiredPrice(request.getPrice(), "LEGACY");
    }

    private void validateParentCreate(CreateProductRequest request) {
        if (request.getParentProductId() != null && !request.getParentProductId().isBlank()) {
            throw new IllegalArgumentException("PARENT must not have parentProductId");
        }
        if (request.getSku() != null && !request.getSku().isBlank()) {
            throw new IllegalArgumentException("PARENT must not have sku");
        }
        if (request.getPrice() != null) {
            validateNonNegativePrice(request.getPrice());
        }
    }

    private void validateSkuCreate(CreateProductRequest request) {
        validateRequiredText(request.getParentProductId(), "parentProductId");
        validateRequiredText(request.getSku(), "sku");
        validateSkuCodeLength(request.getSku());
        validateRequiredPrice(request.getPrice(), "SKU");

        ProductEntity parent = productRepository.findById(request.getParentProductId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Parent product not found: " + request.getParentProductId()));
        if (ProductKind.resolve(parent) != ProductKind.PARENT) {
            throw new IllegalArgumentException("parentProductId must reference a PARENT product");
        }
        if (parent.getShopId() == null || request.getShopId() == null
                || !parent.getShopId().trim().equals(request.getShopId().trim())) {
            throw new IllegalArgumentException("SKU shopId must match parent shopId");
        }
        if (productRepository.existsByShopIdAndSku(request.getShopId().trim(), request.getSku().trim())) {
            throw new IllegalArgumentException("sku already exists for this shop");
        }
    }

    private static void validateRequiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void validateSkuCodeLength(String sku) {
        if (sku.length() > Constants.Product.MAX_SKU_CODE_LENGTH) {
            throw new IllegalArgumentException("sku must be at most " + Constants.Product.MAX_SKU_CODE_LENGTH + " characters");
        }
    }

    private static void validateRequiredPrice(BigDecimal price, String context) {
        if (price == null) {
            throw new IllegalArgumentException("price must not be null for " + context);
        }
        validateNonNegativePrice(price);
    }

    private static void validateNonNegativePrice(BigDecimal price) {
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must be greater than or equal to 0");
        }
    }
}
