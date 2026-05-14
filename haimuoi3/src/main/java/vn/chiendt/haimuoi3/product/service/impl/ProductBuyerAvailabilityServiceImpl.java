package vn.chiendt.haimuoi3.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.chiendt.haimuoi3.common.utils.ShopIdUtils;
import vn.chiendt.haimuoi3.inventory.repository.ProductStockRepository;
import vn.chiendt.haimuoi3.product.model.ProductBuyerAvailability;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.product.service.ProductBuyerAvailabilityService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductBuyerAvailabilityServiceImpl implements ProductBuyerAvailabilityService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    @Override
    public ProductBuyerAvailability resolveAvailabilityByProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            return ProductBuyerAvailability.DISCONTINUED;
        }
        return productRepository.findById(productId.trim())
                .map(this::resolveAvailability)
                .orElse(ProductBuyerAvailability.DISCONTINUED);
    }

    @Override
    public ProductBuyerAvailability resolveAvailability(ProductEntity entity) {
        if (entity == null) {
            return ProductBuyerAvailability.DISCONTINUED;
        }
        String status = entity.getStatus();
        if (status == null || !"ACTIVE".equalsIgnoreCase(status.trim())) {
            return ProductBuyerAvailability.DISCONTINUED;
        }
        Long shopId = ShopIdUtils.parseLongOrNull(entity.getShopId());
        if (shopId == null) {
            return ProductBuyerAvailability.DISCONTINUED;
        }
        ProductKind kind = ProductKind.resolve(entity);
        if (kind == ProductKind.PARENT) {
            return resolveParentAvailability(entity, shopId);
        }
        if (kind == ProductKind.SKU) {
            return resolveSingleSellableUnitStock(shopId, entity.getId());
        }
        return resolveSingleSellableUnitStock(shopId, entity.getId());
    }

    private ProductBuyerAvailability resolveParentAvailability(ProductEntity parent, Long shopId) {
        List<ProductEntity> skus = productRepository.findActiveSkusByParentIdAndShopId(
                parent.getId(), parent.getShopId().trim());
        if (skus.isEmpty()) {
            return ProductBuyerAvailability.OUT_OF_STOCK;
        }
        int total = 0;
        for (ProductEntity sku : skus) {
            total += productStockRepository.findByShopIdAndProductId(shopId, sku.getId())
                    .map(s -> s.getQuantityOnHand() == null ? 0 : Math.max(0, s.getQuantityOnHand()))
                    .orElse(0);
        }
        return total > 0 ? ProductBuyerAvailability.AVAILABLE : ProductBuyerAvailability.OUT_OF_STOCK;
    }

    private ProductBuyerAvailability resolveSingleSellableUnitStock(Long shopId, String productId) {
        int qty = productStockRepository.findByShopIdAndProductId(shopId, productId)
                .map(s -> s.getQuantityOnHand() == null ? 0 : Math.max(0, s.getQuantityOnHand()))
                .orElse(0);
        return qty > 0 ? ProductBuyerAvailability.AVAILABLE : ProductBuyerAvailability.OUT_OF_STOCK;
    }
}
