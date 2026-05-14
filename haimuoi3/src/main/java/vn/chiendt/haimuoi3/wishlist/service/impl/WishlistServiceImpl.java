package vn.chiendt.haimuoi3.wishlist.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.dto.response.ProductPriceRange;
import vn.chiendt.haimuoi3.product.mapper.ProductMapper;
import vn.chiendt.haimuoi3.product.model.ProductBuyerAvailability;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.product.service.ProductBuyerAvailabilityService;
import vn.chiendt.haimuoi3.wishlist.dto.request.WishlistContainsRequest;
import vn.chiendt.haimuoi3.wishlist.dto.response.WishlistContainsResponse;
import vn.chiendt.haimuoi3.wishlist.dto.response.WishlistItemResponse;
import vn.chiendt.haimuoi3.wishlist.model.postgres.WishlistItemEntity;
import vn.chiendt.haimuoi3.wishlist.repository.WishlistItemRepository;
import vn.chiendt.haimuoi3.wishlist.service.WishlistService;
import vn.chiendt.haimuoi3.wishlist.validator.WishlistContainsRequestValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wishlist lưu {@code product_id} theo <strong>sản phẩm cha (PARENT)</strong> trong Mongo;
 * nếu client gửi SKU thì chuẩn hóa về id cha trước khi lưu / so khớp.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductBuyerAvailabilityService productBuyerAvailabilityService;
    private final ProductMapper productMapper;

    @Override
    public List<WishlistItemResponse> listRecent(Long userId, int requestedLimit) {
        int effectiveLimit = clampRecentWishlistLimit(requestedLimit);
        if (log.isDebugEnabled()) {
            log.debug("listRecent wishlist userId={} requestedLimit={} effectiveLimit={}", userId, requestedLimit, effectiveLimit);
        }
        List<WishlistItemEntity> recentWishlistRows = wishlistItemRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, effectiveLimit))
                .getContent();
        return mapWishlistRowsToResponses(recentWishlistRows);
    }

    @Override
    public Page<WishlistItemResponse> listPaged(Long userId, Pageable pageable) {
        if (log.isDebugEnabled()) {
            log.debug("listPaged wishlist userId={} page={} size={}", userId, pageable.getPageNumber(), pageable.getPageSize());
        }
        Page<WishlistItemEntity> wishlistItemPage =
                wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<WishlistItemResponse> responseContent = mapWishlistRowsToResponses(wishlistItemPage.getContent());
        return new PageImpl<>(responseContent, pageable, wishlistItemPage.getTotalElements());
    }

    @Override
    public WishlistContainsResponse contains(Long userId, WishlistContainsRequest request) {
        List<String> requestedProductIds = WishlistContainsRequestValidator.normalizeProductIds(request);
        if (log.isDebugEnabled()) {
            log.debug("contains wishlist userId={} distinctRequestedIds={}", userId, requestedProductIds.size());
        }

        // Anh xa id client (co the la PARENT, SKU, LEGACY) -> id cha luu trong bang wishlist.
        Map<String, String> requestedProductIdToParentProductId = new HashMap<>();
        for (String requestedProductId : requestedProductIds) {
            resolveCanonicalParentProductIdForRead(requestedProductId)
                    .ifPresent(parentProductId ->
                            requestedProductIdToParentProductId.put(requestedProductId, parentProductId));
        }

        if (requestedProductIdToParentProductId.isEmpty()) {
            Map<String, Boolean> allFalse = requestedProductIds.stream()
                    .collect(Collectors.toMap(requestedId -> requestedId, requestedId -> false));
            log.debug("contains wishlist userId={}: không resolve được id nào → tất cả false", userId);
            return WishlistContainsResponse.builder().contains(allFalse).build();
        }

        Set<String> parentProductIdsToLookup = new HashSet<>(requestedProductIdToParentProductId.values());
        Set<String> parentProductIdsInWishlist = wishlistItemRepository
                .findByUserIdAndProductIdIn(userId, parentProductIdsToLookup)
                .stream()
                .map(WishlistItemEntity::getProductId)
                .collect(Collectors.toSet());

        Map<String, Boolean> requestedProductIdToInWishlist = new HashMap<>();
        for (String requestedProductId : requestedProductIds) {
            String parentProductId = requestedProductIdToParentProductId.get(requestedProductId);
            boolean inWishlist = parentProductId != null && parentProductIdsInWishlist.contains(parentProductId);
            requestedProductIdToInWishlist.put(requestedProductId, inWishlist);
        }
        return WishlistContainsResponse.builder().contains(requestedProductIdToInWishlist).build();
    }

    @Override
    @Transactional
    public WishlistItemResponse add(Long userId, String rawProductId) {
        if (!StringUtils.hasText(rawProductId)) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        String trimmedProductId = rawProductId.trim();
        String parentProductIdForWishlistRow = resolveCanonicalParentProductIdForWrite(trimmedProductId);

        Optional<WishlistItemEntity> existingWishlistRow =
                wishlistItemRepository.findByUserIdAndProductId(userId, parentProductIdForWishlistRow);
        if (existingWishlistRow.isPresent()) {
            log.info(
                    "add wishlist idempotent userId={} parentProductId={} (rawProductId={})",
                    userId,
                    parentProductIdForWishlistRow,
                    trimmedProductId);
            return buildWishlistItemResponse(existingWishlistRow.get());
        }

        WishlistItemEntity newWishlistRow = WishlistItemEntity.builder()
                .userId(userId)
                .productId(parentProductIdForWishlistRow)
                .build();
        WishlistItemEntity savedWishlistRow = wishlistItemRepository.save(newWishlistRow);
        log.info(
                "add wishlist userId={} parentProductId={} wishlistRowId={} (rawProductId={})",
                userId,
                parentProductIdForWishlistRow,
                savedWishlistRow.getId(),
                trimmedProductId);
        return buildWishlistItemResponse(savedWishlistRow);
    }

    @Override
    @Transactional
    public void remove(Long userId, String rawProductId) {
        if (!StringUtils.hasText(rawProductId)) {
            log.debug("remove wishlist userId={}: bỏ qua productId rỗng", userId);
            return;
        }
        Optional<String> parentProductIdOptional = resolveCanonicalParentProductIdForRead(rawProductId.trim());
        if (parentProductIdOptional.isEmpty()) {
            log.debug("remove wishlist userId={} rawProductId={}: không resolve được id cha → không xóa", userId, rawProductId);
            return;
        }
        String parentProductId = parentProductIdOptional.get();
        wishlistItemRepository.deleteByUserIdAndProductId(userId, parentProductId);
        log.info("remove wishlist userId={} parentProductId={} (rawProductId={})", userId, parentProductId, rawProductId.trim());
    }

    /**
     * Batch load sản phẩm + khoảng giá SKU (parent) để tránh N+1 khi map nhiều dòng wishlist.
     */
    private List<WishlistItemResponse> mapWishlistRowsToResponses(List<WishlistItemEntity> wishlistRows) {
        if (wishlistRows.isEmpty()) {
            return List.of();
        }
        List<String> wishlistedParentProductIds =
                wishlistRows.stream().map(WishlistItemEntity::getProductId).toList();
        Map<String, ProductEntity> productEntityByProductId = productRepository.findByIdIn(wishlistedParentProductIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product, (first, second) -> first));
        Map<String, ProductPriceRange> activeSkuMinMaxPriceByParentId =
                productRepository.findMinMaxActiveSkuPriceByParentIds(wishlistedParentProductIds);

        List<WishlistItemResponse> responses = new ArrayList<>();
        for (WishlistItemEntity wishlistItemEntity : wishlistRows) {
            ProductEntity catalogProduct = productEntityByProductId.get(wishlistItemEntity.getProductId());
            ProductPriceRange activeSkuPriceRange = activeSkuMinMaxPriceByParentId.get(wishlistItemEntity.getProductId());
            responses.add(buildWishlistItemResponse(wishlistItemEntity, catalogProduct, activeSkuPriceRange));
        }
        return responses;
    }

    private WishlistItemResponse buildWishlistItemResponse(WishlistItemEntity wishlistItemEntity) {
        ProductEntity catalogProduct = productRepository.findById(wishlistItemEntity.getProductId()).orElse(null);
        ProductPriceRange activeSkuPriceRangeForParent = null;
        if (catalogProduct != null && ProductKind.resolve(catalogProduct) == ProductKind.PARENT) {
            activeSkuPriceRangeForParent = productRepository
                    .findMinMaxActiveSkuPriceByParentIds(List.of(catalogProduct.getId()))
                    .get(catalogProduct.getId());
        }
        return buildWishlistItemResponse(wishlistItemEntity, catalogProduct, activeSkuPriceRangeForParent);
    }

    /**
     * @param activeSkuPriceRange chỉ có nghĩa khi {@code catalogProduct} là PARENT; SKU/LEGACY sẽ ghi đè bằng {@code ProductEntity#getPrice()}.
     */
    private WishlistItemResponse buildWishlistItemResponse(
            WishlistItemEntity wishlistItemEntity,
            ProductEntity catalogProduct,
            ProductPriceRange activeSkuPriceRange) {
        if (catalogProduct == null) {
            log.debug(
                    "buildWishlistItemResponse: wishlistRowId={} productId={} không còn document Mongo",
                    wishlistItemEntity.getId(),
                    wishlistItemEntity.getProductId());
            return WishlistItemResponse.builder()
                    .id(wishlistItemEntity.getId())
                    .productId(wishlistItemEntity.getProductId())
                    .name("Sản phẩm không còn tồn tại")
                    .imageUrl(null)
                    .minPrice(null)
                    .maxPrice(null)
                    .availability(ProductBuyerAvailability.DISCONTINUED)
                    .activeSkuCount(0)
                    .addedAt(wishlistItemEntity.getCreatedAt())
                    .build();
        }
        ProductBuyerAvailability buyerAvailability = productBuyerAvailabilityService.resolveAvailability(catalogProduct);
        int activeSkuCountForAddToCartHint = countActiveSkusForDisplay(catalogProduct);
        BigDecimal displayMinPrice = null;
        BigDecimal displayMaxPrice = null;
        if (activeSkuPriceRange != null) {
            displayMinPrice = activeSkuPriceRange.minPrice();
            displayMaxPrice = activeSkuPriceRange.maxPrice();
        }
        if (ProductKind.resolve(catalogProduct) != ProductKind.PARENT) {
            displayMinPrice = catalogProduct.getPrice();
            displayMaxPrice = catalogProduct.getPrice();
        }
        return WishlistItemResponse.builder()
                .id(wishlistItemEntity.getId())
                .productId(wishlistItemEntity.getProductId())
                .name(catalogProduct.getName())
                .imageUrl(productMapper.extractImageUrl(catalogProduct.getProductPictures()))
                .minPrice(displayMinPrice)
                .maxPrice(displayMaxPrice)
                .availability(buyerAvailability)
                .activeSkuCount(activeSkuCountForAddToCartHint)
                .addedAt(wishlistItemEntity.getCreatedAt())
                .build();
    }

    /**
     * Số lượng SKU ACTIVE dưới parent (hoặc 1 với LEGACY/SKU) — FE dùng để quyết định auto-add giỏ hay mở PDP.
     */
    private int countActiveSkusForDisplay(ProductEntity catalogProduct) {
        ProductKind productKind = ProductKind.resolve(catalogProduct);
        if (productKind == ProductKind.PARENT) {
            if (catalogProduct.getShopId() == null || catalogProduct.getShopId().isBlank()) {
                return 0;
            }
            return productRepository
                    .findActiveSkusByParentIdAndShopId(catalogProduct.getId(), catalogProduct.getShopId().trim())
                    .size();
        }
        return 1;
    }

    /**
     * Lưu wishlist theo id cha; nếu là SKU thì tìm parent PARENT hợp lệ.
     */
    private String resolveCanonicalParentProductIdForWrite(String productIdFromClient) {
        ProductEntity catalogProduct = productRepository.findById(productIdFromClient)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productIdFromClient));
        ProductKind productKind = ProductKind.resolve(catalogProduct);
        if (productKind == ProductKind.PARENT || productKind == ProductKind.LEGACY) {
            return catalogProduct.getId();
        }
        if (productKind == ProductKind.SKU) {
            if (!StringUtils.hasText(catalogProduct.getParentProductId())) {
                throw new IllegalArgumentException("SKU product has no parent_product_id");
            }
            String parentProductId = catalogProduct.getParentProductId().trim();
            ProductEntity parentProduct = productRepository.findById(parentProductId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent product not found: " + parentProductId));
            if (ProductKind.resolve(parentProduct) != ProductKind.PARENT) {
                throw new IllegalArgumentException("Wishlist parent must be a PARENT product");
            }
            return parentProduct.getId();
        }
        return catalogProduct.getId();
    }

    /**
     * Giống {@link #resolveCanonicalParentProductIdForWrite(String)} nhưng không ném exception: id không hợp lệ → empty.
     */
    private Optional<String> resolveCanonicalParentProductIdForRead(String productIdFromClient) {
        Optional<ProductEntity> catalogProductOptional = productRepository.findById(productIdFromClient);
        if (catalogProductOptional.isEmpty()) {
            return Optional.empty();
        }
        ProductEntity catalogProduct = catalogProductOptional.get();
        ProductKind productKind = ProductKind.resolve(catalogProduct);
        if (productKind == ProductKind.PARENT || productKind == ProductKind.LEGACY) {
            return Optional.of(catalogProduct.getId());
        }
        if (productKind == ProductKind.SKU) {
            if (!StringUtils.hasText(catalogProduct.getParentProductId())) {
                return Optional.empty();
            }
            String parentProductId = catalogProduct.getParentProductId().trim();
            return productRepository.findById(parentProductId)
                    .filter(parent -> ProductKind.resolve(parent) == ProductKind.PARENT)
                    .map(ProductEntity::getId);
        }
        return Optional.of(catalogProduct.getId());
    }

    private static int clampRecentWishlistLimit(int requestedLimit) {
        if (requestedLimit < 1) {
            return Constants.Wishlist.DEFAULT_RECENT_LIMIT;
        }
        return Math.min(requestedLimit, Constants.Wishlist.MAX_RECENT_LIMIT);
    }
}
