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
import vn.chiendt.haimuoi3.shop.validator.CreateShopCategoryRequestValidator;

@Service
@RequiredArgsConstructor
public class ShopCategoryServiceImpl implements ShopCategoryService {

    private final ShopCategoryRepository shopCategoryRepository;
    private final GlobalCategoryRepository globalCategoryRepository;
    private final ShopService shopService;
    private final CreateShopCategoryRequestValidator createShopCategoryRequestValidator;

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
