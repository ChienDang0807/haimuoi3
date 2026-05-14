package vn.chiendt.haimuoi3.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.media.dto.response.MediaUploadResponse;
import vn.chiendt.haimuoi3.media.model.MediaTargetType;
import vn.chiendt.haimuoi3.media.service.MediaService;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.common.utils.ShopIdUtils;
import vn.chiendt.haimuoi3.product.dto.request.CreateProductRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateProductRequest;
import vn.chiendt.haimuoi3.product.dto.response.CartProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.ProductPriceRange;
import vn.chiendt.haimuoi3.product.dto.response.ProductSuggestionResponse;
import vn.chiendt.haimuoi3.product.dto.response.ShopProductResponse;
import vn.chiendt.haimuoi3.product.mapper.ProductMapper;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductPicture;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.product.service.ProductService;
import vn.chiendt.haimuoi3.product.validator.ProductBusinessValidator;
import vn.chiendt.haimuoi3.product.validator.ProductParentSkuValidator;
import vn.chiendt.haimuoi3.product.repository.GlobalCategoryRepository;
import vn.chiendt.haimuoi3.shop.model.ShopCategoryEntity;
import vn.chiendt.haimuoi3.shop.repository.ShopCategoryRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductBusinessValidator productBusinessValidator;
    private final ProductParentSkuValidator productParentSkuValidator;
    private final MediaService mediaService;
    private final InventoryService inventoryService;
    private final ShopCategoryRepository shopCategoryRepository;
    private final GlobalCategoryRepository globalCategoryRepository;

    @Override
    public Page<GlobalProductResponse> findAllGlobalProduct(
            String query,
            String globalCategoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable
    ) {
        productBusinessValidator.validateGlobalFilter(query, minPrice, maxPrice, minRating);
        Page<ProductEntity> entityPage = productRepository.findGlobalProducts(
                query,
                globalCategoryId,
                minPrice,
                maxPrice,
                minRating,
                pageable
        );
        Page<GlobalProductResponse> mapped = entityPage.map(productMapper::toGlobalResponse);
        List<String> parentIds = new ArrayList<>();
        for (int i = 0; i < entityPage.getContent().size(); i++) {
            ProductEntity e = entityPage.getContent().get(i);
            if (ProductKind.resolve(e) == ProductKind.PARENT) {
                parentIds.add(e.getId());
            }
        }
        Map<String, ProductPriceRange> ranges = productRepository.findMinMaxActiveSkuPriceByParentIds(parentIds);
        for (int i = 0; i < mapped.getContent().size(); i++) {
            GlobalProductResponse g = mapped.getContent().get(i);
            ProductEntity src = entityPage.getContent().get(i);
            if (ProductKind.resolve(src) != ProductKind.PARENT) {
                continue;
            }
            ProductPriceRange r = ranges.get(g.getId());
            if (r != null) {
                g.setMinSkuPrice(r.minPrice());
                g.setMaxSkuPrice(r.maxPrice());
                if (g.getPrice() == null || g.getPrice().signum() == 0) {
                    g.setPrice(r.minPrice());
                }
            }
        }
        return mapped;
    }

    @Override
    public List<ProductSuggestionResponse> suggestProducts(String query, Integer limit) {
        productBusinessValidator.validateSuggestQuery(query);
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int normalizedLimit = productBusinessValidator.normalizeSuggestLimit(limit);
        return productRepository.suggestProducts(query, normalizedLimit)
                .stream()
                .map(productMapper::toSuggestionResponse)
                .toList();
    }

    @Override
    public ShopProductResponse findActiveProductForPublic(String id) {
        ProductEntity entity = findProductEntityById(id);
        String status = entity.getStatus();
        if (status == null || !"ACTIVE".equalsIgnoreCase(status.trim())) {
            throw new ResourceNotFoundException("Product not found or not available");
        }
        ProductKind kind = ProductKind.resolve(entity);
        if (kind == ProductKind.PARENT) {
            return buildPublicParentDetail(entity);
        }
        if (kind == ProductKind.SKU) {
            return buildPublicSkuDetail(entity);
        }
        return productMapper.toShopResponse(entity);
    }

    @Override
    public List<ShopProductResponse> listActiveSkusByParentForPublic(String parentId) {
        ProductEntity parent = findProductEntityById(parentId);
        if (ProductKind.resolve(parent) != ProductKind.PARENT) {
            throw new IllegalArgumentException("parentId must reference a PARENT product");
        }
        String st = parent.getStatus();
        if (st == null || !"ACTIVE".equalsIgnoreCase(st.trim())) {
            throw new ResourceNotFoundException("Product not found or not available");
        }
        if (parent.getShopId() == null || parent.getShopId().isBlank()) {
            throw new IllegalArgumentException("parent has no shopId");
        }
        return productRepository.findActiveSkusByParentIdAndShopId(parentId, parent.getShopId().trim())
                .stream()
                .map(productMapper::toShopResponse)
                .toList();
    }

    @Override
    public List<CartProductResponse> findCartProductsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<String> dedupedIds = new ArrayList<>(new LinkedHashSet<>(ids));
        return productRepository.findByIdIn(dedupedIds)
                .stream()
                .filter(p -> ProductKind.resolve(p) != ProductKind.PARENT)
                .map(productMapper::toCartProductResponse)
                .toList();
    }

    @Override
    public Page<ShopProductResponse> findAllShopProduct(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toShopResponse);
    }

    @Override
    public ShopProductResponse findById(String id) {
        ProductEntity product = findProductEntityById(id);
        return productMapper.toShopResponse(product);
    }

    @Override
    public ShopProductResponse save(CreateProductRequest request) {
        productBusinessValidator.validateCreate(request);

        if (StringUtils.hasText(request.getShopCategoryId())) {
            request.setCategoryPublicId(null);
        }

        ProductEntity entity = productMapper.toEntity(request);
        applyShopCategoryToProduct(entity, request.getShopCategoryId().trim(), request.getShopId().trim());

        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("ACTIVE");
        }
        if (ProductKind.resolve(entity) == ProductKind.PARENT
                && (entity.getPrice() == null || entity.getPrice().signum() < 0)) {
            entity.setPrice(BigDecimal.ZERO);
        }
        ProductEntity savedEntity = productRepository.save(entity);
        long shopId = ShopIdUtils.requireLongShopId(request.getShopId(), "shopId must be a numeric shop id");
        if (ProductKind.resolve(savedEntity) == ProductKind.SKU || ProductKind.resolve(savedEntity) == ProductKind.LEGACY) {
            inventoryService.ensureProductStockRow(shopId, savedEntity.getId());
        }
        return productMapper.toShopResponse(savedEntity);
    }

    private void applyShopCategoryToProduct(ProductEntity entity, String shopCategoryId, String shopId) {
        ShopCategoryEntity sc = shopCategoryRepository.findById(shopCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop category not found: " + shopCategoryId));
        if (sc.getShopId() == null || !sc.getShopId().trim().equals(shopId)) {
            throw new IllegalArgumentException("shopCategoryId does not belong to this shop");
        }
        entity.setShopCategoryId(sc.getShopCategoryId());
        entity.setShopCategoryName(sc.getName());
        if (StringUtils.hasText(sc.getGlobalCategoryId())) {
            String gid = sc.getGlobalCategoryId().trim();
            entity.setGlobalCategoryId(gid);
            globalCategoryRepository.findById(gid)
                    .ifPresent(g -> entity.setGlobalCategoryName(g.getName()));
        } else {
            entity.setGlobalCategoryId(null);
            entity.setGlobalCategoryName(null);
        }
    }

    @Override
    @Transactional
    public ShopProductResponse update(String id, UpdateProductRequest request) {
        productBusinessValidator.validateUpdate(request);

        ProductEntity existing = findProductEntityById(id);
        productParentSkuValidator.validateUpdate(existing, request);
        applyUpdate(existing, request);

        ProductEntity updated = productRepository.save(existing);
        return productMapper.toShopResponse(updated);
    }

    @Override
    public ShopProductResponse uploadProductImage(String id, MultipartFile file) {
        ProductEntity existing = findProductEntityById(id);
        MediaUploadResponse uploaded = mediaService.uploadImage(file, MediaTargetType.PRODUCT);
        appendProductImage(existing, uploaded);
        ProductEntity updated = productRepository.save(existing);
        return productMapper.toShopResponse(updated);
    }

    @Override
    public ShopProductResponse uploadProductImages(String id, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("files must not be empty");
        }
        ProductEntity existing = findProductEntityById(id);
        for (MultipartFile file : files) {
            MediaUploadResponse uploaded = mediaService.uploadImage(file, MediaTargetType.PRODUCT);
            appendProductImage(existing, uploaded);
        }
        ProductEntity updated = productRepository.save(existing);
        return productMapper.toShopResponse(updated);
    }

    @Override
    public void delete(String id) {
        ProductEntity existing = findProductEntityById(id);
        productRepository.delete(existing);
    }

    @Override
    public Page<ShopProductResponse> findAllByShopId(String shopId, Pageable pageable) {
        return productRepository.findCatalogByShopId(shopId, pageable).map(productMapper::toShopResponse);
    }

    private ProductEntity findProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void applyUpdate(ProductEntity existing, UpdateProductRequest request) {
        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getBrand() != null) {
            existing.setBrand(request.getBrand());
        }
        if (request.getCategoryPublicId() != null) {
            existing.setGlobalCategoryId(request.getCategoryPublicId());
        }
        if (request.getFeatured() != null) {
            existing.setFeatured(request.getFeatured());
        }
        if (request.getBadgeType() != null) {
            existing.setBadgeType(request.getBadgeType());
        }
        if (request.getAttributes() != null) {
            existing.setAttributes(productMapper.toProductAttributes(request.getAttributes()));
        }
        if (request.getPictures() != null) {
            existing.setProductPictures(productMapper.toProductPictures(request.getPictures()));
        }
        if (request.getProductKind() != null) {
            existing.setProductKind(request.getProductKind());
        }
        if (request.getParentProductId() != null) {
            existing.setParentProductId(request.getParentProductId().isBlank() ? null : request.getParentProductId().trim());
        }
        if (request.getSku() != null) {
            existing.setSku(request.getSku().isBlank() ? null : request.getSku().trim());
        }
        if (request.getVariantOptions() != null) {
            existing.setVariantOptions(request.getVariantOptions());
        }
    }

    private ShopProductResponse buildPublicParentDetail(ProductEntity parent) {
        ShopProductResponse response = productMapper.toShopResponse(parent);
        if (parent.getShopId() == null || parent.getShopId().isBlank()) {
            return response;
        }
        List<ProductEntity> skus = productRepository.findActiveSkusByParentIdAndShopId(parent.getId(), parent.getShopId().trim());
        List<ShopProductResponse> skuDtos = skus.stream().map(productMapper::toShopResponse).toList();
        response.setSkus(skuDtos);
        if (!skus.isEmpty()) {
            BigDecimal min = skus.stream().map(ProductEntity::getPrice).min(BigDecimal::compareTo).orElse(null);
            BigDecimal max = skus.stream().map(ProductEntity::getPrice).max(BigDecimal::compareTo).orElse(null);
            response.setMinSkuPrice(min);
            response.setMaxSkuPrice(max);
        }
        return response;
    }

    private ShopProductResponse buildPublicSkuDetail(ProductEntity sku) {
        ShopProductResponse response = productMapper.toShopResponse(sku);
        String parentId = sku.getParentProductId();
        if (parentId == null || parentId.isBlank()) {
            return response;
        }
        productRepository.findById(parentId.trim()).ifPresent(parent -> {
            if (ProductKind.resolve(parent) == ProductKind.PARENT) {
                if (response.getDescription() == null || response.getDescription().isBlank()) {
                    response.setDescription(parent.getDescription());
                }
                if ((response.getProductPictures() == null || response.getProductPictures().isEmpty())
                        && parent.getProductPictures() != null && !parent.getProductPictures().isEmpty()) {
                    response.setProductPictures(new ArrayList<>(parent.getProductPictures()));
                }
                if (response.getGlobalCategoryId() == null && parent.getGlobalCategoryId() != null) {
                    response.setGlobalCategoryId(parent.getGlobalCategoryId());
                }
                if (response.getGlobalCategoryName() == null && parent.getGlobalCategoryName() != null) {
                    response.setGlobalCategoryName(parent.getGlobalCategoryName());
                }
            }
        });
        return response;
    }

    private void appendProductImage(ProductEntity entity, MediaUploadResponse uploaded) {
        List<ProductPicture> pictures = entity.getProductPictures();
        if (pictures == null) {
            pictures = new ArrayList<>();
            entity.setProductPictures(pictures);
        }
        ProductPicture picture = ProductPicture.builder()
                .url(uploaded.getUrl())
                .mimiType(uploaded.getContentType())
                .build();
        pictures.add(picture);
    }
}
