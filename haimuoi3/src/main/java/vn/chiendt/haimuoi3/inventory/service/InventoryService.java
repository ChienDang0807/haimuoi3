package vn.chiendt.haimuoi3.inventory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.inventory.dto.request.AdjustStockRequest;
import vn.chiendt.haimuoi3.inventory.dto.response.InventoryItemResponse;
import vn.chiendt.haimuoi3.inventory.model.postgres.ProductStockEntity;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

public interface InventoryService {

    void assertAvailabilityForCreateOrder(CreateOrderRequest request);

    void applyAfterOrderPlaced(OrderEntity savedOrder);

    void commitHeldForOrder(Long orderId);

    void releaseHeldForOrder(Long orderId);

    /**
     * Khach huy don COD da CONFIRMED: hoan kho theo reservation COMMITTED, hoac theo order_items neu don cu.
     */
    void restockAfterCustomerCancelConfirmedCod(Long orderId, OrderStatus previousStatus, String paymentMethod);

    void ensureProductStockRow(Long shopId, String productId);

    /**
     * Get paginated inventory items scoped to a shop.
     * Each item includes productId, displayName, sku, quantityOnHand, and lowStock flag.
     */
    Page<InventoryItemResponse> getInventoryByShopId(Long shopId, Pageable pageable);

    Page<InventoryItemResponse> getInventoryForShopOwner(Long ownerUserId, Pageable pageable);

    InventoryItemResponse adjustStockForShopOwner(Long ownerUserId, String productId, AdjustStockRequest request);

    /**
     * Adjust the quantity on hand for a product in the given shop.
     * Validates that the product belongs to the shop.
     *
     * @param shopId         the shop ID
     * @param productId      the product ID
     * @param quantityOnHand the new absolute quantity on hand (must be >= 0)
     * @return the updated ProductStockEntity
     */
    ProductStockEntity adjustStock(Long shopId, String productId, int quantityOnHand);
}
