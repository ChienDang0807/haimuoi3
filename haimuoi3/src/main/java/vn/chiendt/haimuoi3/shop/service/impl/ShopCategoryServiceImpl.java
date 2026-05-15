package vn.chiendt.haimuoi3.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.model.postgres.GlobalCategoryEntity;
import vn.chiendt.haimuoi3.product.repository.GlobalCategoryRepository;
import vn.chiendt.haimuoi3.shop.dto.request.CreateShopCategoryRequest;
import vn.chiendt.haimuoi3.shop.dto.response.ShopCategoryResponse;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.model.ShopCategoryEntity;
import vn.chiendt.haimuoi3.shop.repository.ShopCategoryRepository;
import vn.chiendt.haimuoi3.shop.service.ShopCategoryService;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.dto.request.UpdateShopCategoryRequest;
import vn.chiendt.haimuoi3.shop.validator.CreateShopCategoryRequestValidator;

@Service
@RequiredArgsConstructor
public class ShopCategoryServiceImpl implements ShopCategoryService {

    private final ShopCategoryRepository shopCategoryRepository;
    private final GlobalCategoryRepository globalCategoryRepository;
    private final ShopService shopService;
    private final CreateShopCategoryRequestValidator createShopCategoryRequestValidator;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ShopCategoryResponse> listForShopOwner(Long ownerUserId, Pageable pageable) {
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        String shopId = String.valueOf(shop.getId());
        return shopCategoryRepository.findByShopIdOrderByDisplayOrderAsc(shopId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ShopCategoryResponse createShopCategory(Long ownerUserId, CreateShopCategoryRequest request) {
        createShopCategoryRequestValidator.validate(request);
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        String shopId = String.valueOf(shop.getId());
        String slug = request.getSlug().trim();
        if (shopCategoryRepository.existsByShopIdAndSlug(shopId, slug)) {
            throw new IllegalArgumentException("Slug already exists for this shop: " + slug);
        }

        final String globalId;
        if (StringUtils.hasText(request.getGlobalCategoryId())) {
            String trimmedGlobalId = request.getGlobalCategoryId().trim();
            globalCategoryRepository.findById(trimmedGlobalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Global category not found: " + trimmedGlobalId));
            globalId = trimmedGlobalId;
        } else {
            globalId = null;
        }

        int displayOrder = request.getDisplayOrder() != null
                ? request.getDisplayOrder()
                : (int) shopCategoryRepository.countByShopId(shopId);
        boolean active = request.getIsActive() == null || request.getIsActive();

        ShopCategoryEntity entity = ShopCategoryEntity.builder()
                .shopId(shopId)
                .globalCategoryId(globalId)
                .name(request.getName().trim())
                .slug(slug)
                .imageUrl(StringUtils.hasText(request.getImageUrl()) ? request.getImageUrl().trim() : null)
                .displayOrder(displayOrder)
                .isActive(active)
                .build();

        ShopCategoryEntity saved = shopCategoryRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ShopCategoryResponse updateShopCategory(Long ownerUserId, String shopCategoryId, UpdateShopCategoryRequest request) {
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        String shopId = String.valueOf(shop.getId());

        ShopCategoryEntity entity = shopCategoryRepository.findById(shopCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop category not found: " + shopCategoryId));

        if (!entity.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("Permission denied");
        }

        if (StringUtils.hasText(request.getName())) {
            entity.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getSlug())) {
            String slug = request.getSlug().trim();
            if (!entity.getSlug().equals(slug) && shopCategoryRepository.existsByShopIdAndSlug(shopId, slug)) {
                throw new IllegalArgumentException("Slug already exists: " + slug);
            }
            entity.setSlug(slug);
        }
        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
        if (request.getGlobalCategoryId() != null) {
            if (StringUtils.hasText(request.getGlobalCategoryId())) {
                String gid = request.getGlobalCategoryId().trim();
                globalCategoryRepository.findById(gid)
                        .orElseThrow(() -> new ResourceNotFoundException("Global category not found: " + gid));
                entity.setGlobalCategoryId(gid);
            } else {
                entity.setGlobalCategoryId(null);
            }
        }

        ShopCategoryEntity saved = shopCategoryRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ShopCategoryResponse toggleActive(Long ownerUserId, String shopCategoryId) {
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        String shopId = String.valueOf(shop.getId());

        ShopCategoryEntity entity = shopCategoryRepository.findById(shopCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop category not found: " + shopCategoryId));

        if (!entity.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("Permission denied");
        }

        entity.setActive(!entity.isActive());
        ShopCategoryEntity saved = shopCategoryRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteShopCategory(Long ownerUserId, String shopCategoryId) {
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        String shopId = String.valueOf(shop.getId());

        ShopCategoryEntity entity = shopCategoryRepository.findById(shopCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop category not found: " + shopCategoryId));

        if (!entity.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("Permission denied");
        }

        if (productRepository.existsByShopCategoryId(shopCategoryId)) {
            throw new IllegalStateException("Cannot delete category as it is still referenced by products");
        }

        shopCategoryRepository.delete(entity);
    }

    private ShopCategoryResponse toResponse(ShopCategoryEntity e) {
        String globalName = null;
        if (StringUtils.hasText(e.getGlobalCategoryId())) {
            globalName = globalCategoryRepository.findById(e.getGlobalCategoryId().trim())
                    .map(GlobalCategoryEntity::getName)
                    .orElse(null);
        }
        return ShopCategoryResponse.builder()
                .shopCategoryId(e.getShopCategoryId())
                .shopId(e.getShopId())
                .globalCategoryId(e.getGlobalCategoryId())
                .globalCategoryName(globalName)
                .name(e.getName())
                .slug(e.getSlug())
                .imageUrl(e.getImageUrl())
                .displayOrder(e.getDisplayOrder())
                .active(e.isActive())
                .build();
    }
}
