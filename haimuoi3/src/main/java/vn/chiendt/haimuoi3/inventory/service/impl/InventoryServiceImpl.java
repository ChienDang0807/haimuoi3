package vn.chiendt.haimuoi3.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.common.utils.ShopIdUtils;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.inventory.dto.request.AdjustStockRequest;
import vn.chiendt.haimuoi3.inventory.dto.response.InventoryItemResponse;
import vn.chiendt.haimuoi3.inventory.model.postgres.ProductStockEntity;
import vn.chiendt.haimuoi3.inventory.model.postgres.ReservationStatus;
import vn.chiendt.haimuoi3.inventory.model.postgres.StockReservationEntity;
import vn.chiendt.haimuoi3.inventory.repository.ProductStockRepository;
import vn.chiendt.haimuoi3.inventory.repository.StockReservationRepository;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.inventory.validator.AdjustStockRequestValidator;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.OrderItemRequest;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderItemEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.service.ShopService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final StockReservationRepository stockReservationRepository;
    private final OrderRepository orderRepository;
    private final ShopService shopService;
    private final AdjustStockRequestValidator adjustStockRequestValidator;

    @Override
    @Transactional
    public void assertAvailabilityForCreateOrder(CreateOrderRequest request) {
        Long shopId = request.getShopId();
        Map<String, Integer> neededByProduct = aggregateRequestedQuantities(request.getItems());
        for (String productId : new TreeMap<>(neededByProduct).keySet()) {
            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
            if (ProductKind.resolve(product) == ProductKind.PARENT) {
                throw new IllegalArgumentException(Constants.Product.PARENT_NOT_SELLABLE);
            }
            validateProductBelongsToShop(product, shopId);
            ProductStockEntity stock = ensureRowThenLock(shopId, productId);
            int heldElsewhere = stockReservationRepository.sumQuantityByShopIdAndProductIdAndStatus(
                    shopId, productId, ReservationStatus.HELD);
            int need = neededByProduct.get(productId);
            if (stock.getQuantityOnHand() - heldElsewhere < need) {
                throw new IllegalArgumentException(Constants.Inventory.INSUFFICIENT_STOCK);
            }
        }
    }

    @Override
    @Transactional
    public void applyAfterOrderPlaced(OrderEntity savedOrder) {
        if (savedOrder.getItems() == null || savedOrder.getItems().isEmpty()) {
            throw new IllegalStateException("order has no items after save");
        }
        boolean stripe = "STRIPE".equalsIgnoreCase(savedOrder.getPaymentMethod());
        LocalDateTime now = LocalDateTime.now();
        Long shopId = savedOrder.getShopId();
        if (stripe) {
            List<StockReservationEntity> toSave = new ArrayList<>();
            List<OrderItemEntity> sorted = savedOrder.getItems().stream()
                    .sorted(Comparator.comparing(OrderItemEntity::getProductId).thenComparing(OrderItemEntity::getId))
                    .toList();
            for (OrderItemEntity item : sorted) {
                productStockRepository.ensureRowExists(shopId, item.getProductId());
                toSave.add(StockReservationEntity.builder()
                        .orderId(savedOrder.getId())
                        .orderItemId(item.getId())
                        .shopId(shopId)
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .status(ReservationStatus.HELD)
                        .createdAt(now)
                        .build());
            }
            stockReservationRepository.saveAll(toSave);
            return;
        }
        Map<String, Integer> decrementByProduct = savedOrder.getItems().stream()
                .collect(Collectors.groupingBy(
                        OrderItemEntity::getProductId,
                        TreeMap::new,
                        Collectors.summingInt(OrderItemEntity::getQuantity)));
        for (Map.Entry<String, Integer> e : decrementByProduct.entrySet()) {
            ProductStockEntity stock = ensureRowThenLock(shopId, e.getKey());
            int next = stock.getQuantityOnHand() - e.getValue();
            if (next < 0) {
                throw new IllegalStateException(Constants.Inventory.INSUFFICIENT_STOCK);
            }
            stock.setQuantityOnHand(next);
            stock.setUpdatedAt(now);
            productStockRepository.save(stock);
        }
        List<StockReservationEntity> toSave = new ArrayList<>();
        for (OrderItemEntity item : savedOrder.getItems()) {
            toSave.add(StockReservationEntity.builder()
                    .orderId(savedOrder.getId())
                    .orderItemId(item.getId())
                    .shopId(shopId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.COMMITTED)
                    .createdAt(now)
                    .build());
        }
        stockReservationRepository.saveAll(toSave);
    }

    @Override
    @Transactional
    public void commitHeldForOrder(Long orderId) {
        List<StockReservationEntity> held = stockReservationRepository.findByOrderIdAndStatus(
                orderId, ReservationStatus.HELD);
        if (held.isEmpty()) {
            return;
        }
        Long shopId = held.get(0).getShopId();
        Map<String, Integer> qtyByProduct = held.stream()
                .collect(Collectors.groupingBy(
                        StockReservationEntity::getProductId,
                        TreeMap::new,
                        Collectors.summingInt(StockReservationEntity::getQuantity)));
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, Integer> e : qtyByProduct.entrySet()) {
            ProductStockEntity stock = ensureRowThenLock(shopId, e.getKey());
            int next = stock.getQuantityOnHand() - e.getValue();
            if (next < 0) {
                throw new IllegalStateException(Constants.Inventory.INSUFFICIENT_STOCK);
            }
            stock.setQuantityOnHand(next);
            stock.setUpdatedAt(now);
            productStockRepository.save(stock);
        }
        for (StockReservationEntity r : held) {
            r.setStatus(ReservationStatus.COMMITTED);
        }
        stockReservationRepository.saveAll(held);
    }

    @Override
    @Transactional
    public void releaseHeldForOrder(Long orderId) {
        List<StockReservationEntity> held = stockReservationRepository.findByOrderIdAndStatus(
                orderId, ReservationStatus.HELD);
        if (held.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (StockReservationEntity r : held) {
            r.setStatus(ReservationStatus.RELEASED);
            r.setReleasedAt(now);
        }
        stockReservationRepository.saveAll(held);
    }

    @Override
    @Transactional
    public void restockAfterCustomerCancelConfirmedCod(Long orderId, OrderStatus previousStatus, String paymentMethod) {
        if (previousStatus != OrderStatus.CONFIRMED) {
            return;
        }
        if (paymentMethod == null || !"COD".equalsIgnoreCase(paymentMethod.trim())) {
            return;
        }
        List<StockReservationEntity> committed = stockReservationRepository.findByOrderIdAndStatus(
                orderId, ReservationStatus.COMMITTED);
        LocalDateTime now = LocalDateTime.now();
        if (!committed.isEmpty()) {
            Long shopId = committed.get(0).getShopId();
            Map<String, Integer> restoreByProduct = committed.stream()
                    .collect(Collectors.groupingBy(
                            StockReservationEntity::getProductId,
                            TreeMap::new,
                            Collectors.summingInt(StockReservationEntity::getQuantity)));
            for (Map.Entry<String, Integer> e : restoreByProduct.entrySet()) {
                ProductStockEntity stock = ensureRowThenLock(shopId, e.getKey());
                stock.setQuantityOnHand(stock.getQuantityOnHand() + e.getValue());
                stock.setUpdatedAt(now);
                productStockRepository.save(stock);
            }
            for (StockReservationEntity r : committed) {
                r.setStatus(ReservationStatus.RELEASED);
                r.setReleasedAt(now);
            }
            stockReservationRepository.saveAll(committed);
            return;
        }
        OrderEntity order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        Long shopId = order.getShopId();
        Map<String, Integer> restoreByProduct = order.getItems().stream()
                .collect(Collectors.groupingBy(
                        OrderItemEntity::getProductId,
                        TreeMap::new,
                        Collectors.summingInt(OrderItemEntity::getQuantity)));
        for (Map.Entry<String, Integer> e : restoreByProduct.entrySet()) {
            ProductStockEntity stock = ensureRowThenLock(shopId, e.getKey());
            stock.setQuantityOnHand(stock.getQuantityOnHand() + e.getValue());
            stock.setUpdatedAt(now);
            productStockRepository.save(stock);
        }
    }

    @Override
    @Transactional
    public void ensureProductStockRow(Long shopId, String productId) {
        if (shopId == null || shopId <= 0 || productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("shopId and productId must be valid");
        }
        productStockRepository.ensureRowExists(shopId, productId);
    }

    @Override
    @Transactional
    public ProductStockEntity adjustStock(Long shopId, String productId, int quantityOnHand) {
        adjustStockRequestValidator.validateQuantityOnHand(quantityOnHand);
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        validateProductBelongsToShop(product, shopId);

        // Ensure row exists then lock for update
        ProductStockEntity stock = ensureRowThenLock(shopId, productId);
        stock.setQuantityOnHand(quantityOnHand);
        stock.setUpdatedAt(LocalDateTime.now());
        return productStockRepository.save(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> getInventoryForShopOwner(Long ownerUserId, Pageable pageable) {
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        return getInventoryByShopId(shop.getId(), pageable);
    }

    @Override
    @Transactional
    public InventoryItemResponse adjustStockForShopOwner(Long ownerUserId, String productId, AdjustStockRequest request) {
        adjustStockRequestValidator.validate(request);
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        ProductStockEntity updated = adjustStock(shop.getId(), productId, request.getQuantityOnHand());
        return toInventoryItemResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> getInventoryByShopId(Long shopId, Pageable pageable) {
        Page<ProductStockEntity> stockPage = productStockRepository.findByShopId(shopId, pageable);

        // Collect product IDs to resolve names and SKUs from MongoDB
        List<String> productIds = stockPage.getContent().stream()
                .map(ProductStockEntity::getProductId)
                .collect(Collectors.toList());

        Map<String, ProductEntity> productMap = productIds.isEmpty()
                ? Map.of()
                : productRepository.findByIdIn(productIds).stream()
                        .collect(Collectors.toMap(ProductEntity::getId, p -> p, (a, b) -> a));

        return stockPage.map(stock -> toInventoryItemResponse(stock, productMap.get(stock.getProductId())));
    }

    private InventoryItemResponse toInventoryItemResponse(ProductStockEntity stock) {
        ProductEntity product = productRepository.findById(stock.getProductId()).orElse(null);
        return toInventoryItemResponse(stock, product);
    }

    private InventoryItemResponse toInventoryItemResponse(ProductStockEntity stock, ProductEntity product) {
        String displayName = product != null ? product.getName() : "Sản phẩm #" + stock.getProductId();
        String sku = product != null ? product.getSku() : null;
        boolean lowStock = stock.getQuantityOnHand() <= Constants.Dashboard.LOW_STOCK_THRESHOLD;

        return InventoryItemResponse.builder()
                .productId(stock.getProductId())
                .displayName(displayName)
                .sku(sku)
                .quantityOnHand(stock.getQuantityOnHand())
                .lowStock(lowStock)
                .build();
    }

    private ProductStockEntity ensureRowThenLock(Long shopId, String productId) {
        productStockRepository.ensureRowExists(shopId, productId);
        return loadLockedStock(shopId, productId);
    }

    private ProductStockEntity loadLockedStock(Long shopId, String productId) {
        return productStockRepository.findByShopIdAndProductIdForUpdate(shopId, productId)
                .orElseThrow(() -> new IllegalStateException("product_stock row missing after ensure"));
    }

    private static void validateProductBelongsToShop(ProductEntity product, Long shopId) {
        Long productShop = ShopIdUtils.parseLongOrNull(product.getShopId());
        if (productShop == null || productShop != shopId) {
            throw new IllegalArgumentException(Constants.Inventory.PRODUCT_SHOP_MISMATCH);
        }
    }

    private static Map<String, Integer> aggregateRequestedQuantities(List<OrderItemRequest> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        OrderItemRequest::getProductId,
                        TreeMap::new,
                        Collectors.summingInt(OrderItemRequest::getQuantity)));
    }
}
