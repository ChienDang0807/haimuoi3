package vn.chiendt.haimuoi3.order.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.inventory.model.postgres.ProductStockEntity;
import vn.chiendt.haimuoi3.inventory.model.postgres.ReservationStatus;
import vn.chiendt.haimuoi3.inventory.model.postgres.StockReservationEntity;
import vn.chiendt.haimuoi3.inventory.repository.ProductStockRepository;
import vn.chiendt.haimuoi3.inventory.repository.StockReservationRepository;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderItemEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.repository.ShopRepository;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Seed dev: {@code product_stock} (số lượng dương), {@code orders}, {@code order_items},
 * và {@code stock_reservations} trạng thái COMMITTED — khớp trừ kho như đơn COD đã xác nhận.
 */
@Component
@Profile("dev")
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class OrderDemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_SHOP_ID_STRING = "1";
    private static final long DEMO_SHOP_PK = 1L;
    private static final String CUSTOMER_EMAIL = "customer1@mail.com";
    private static final int INITIAL_STOCK_PER_PRODUCT = 1_000;
    /** Cùng batch cho mọi đơn seed — dùng để idempotent, không chặn bởi đơn thật khác. */
    private static final UUID ORDER_DEMO_CHECKOUT_BATCH = UUID.fromString("f0000001-0001-4000-8000-000000000001");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final ProductStockRepository productStockRepository;
    private final StockReservationRepository stockReservationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("OrderDemoDataSeeder: checking (profile must include dev for this bean to load)...");
            if (orderRepository.existsByCheckoutBatchId(ORDER_DEMO_CHECKOUT_BATCH)) {
                log.info("Skip order demo seed: demo orders already exist (checkout_batch_id={})", ORDER_DEMO_CHECKOUT_BATCH);
                return;
            }
            UserEntity customer = userRepository.findByEmail(CUSTOMER_EMAIL)
                    .orElse(null);
            if (customer == null) {
                log.warn("Skip order demo seed: user {} not found", CUSTOMER_EMAIL);
                return;
            }

            SellablePick pick = resolveSellablePick();
            if (pick.products().size() < 2) {
                log.warn(
                        "Skip order demo seed: need at least 2 sellable Mongo products (ACTIVE or null status, not PARENT). "
                                + "shopId='{}' sellable={}, total Mongo products={}. "
                                + "Kiểm tra BSON shopId (string '1' hoặc số 1), field status, product_kind.",
                        DEMO_SHOP_ID_STRING,
                        countSellableForShopIdString(DEMO_SHOP_ID_STRING),
                        productRepository.count()
                );
                return;
            }

            long shopPk = pick.postgresShopId();
            if (!shopRepository.existsById(shopPk)) {
                log.warn("Skip order demo seed: Postgres shop id {} (from Mongo shopId) not found", shopPk);
                return;
            }

            List<ProductEntity> sellable = pick.products();
            log.info(
                    "OrderDemoDataSeeder: using Postgres shop_id={} for {} demo line items (Mongo shopId={})",
                    shopPk,
                    sellable.size(),
                    normalizeShopId(sellable.get(0))
            );

            for (ProductEntity p : sellable) {
                upsertStock(shopPk, p.getId(), INITIAL_STOCK_PER_PRODUCT);
            }

            LocalDateTime base = LocalDateTime.now().minusDays(3);
            OrderEntity order1 = buildOrder(
                    shopPk,
                    customer.getId(),
                    customer.getFullName(),
                    base,
                    OrderStatus.CONFIRMED,
                    ORDER_DEMO_CHECKOUT_BATCH,
                    List.of(
                            lineSpec(sellable.get(0), 2),
                            lineSpec(sellable.get(1), 1)
                    )
            );
            orderRepository.save(order1);
            applyCommittedInventory(order1);

            if (sellable.size() >= 3) {
                OrderEntity order2 = buildOrder(
                        shopPk,
                        customer.getId(),
                        customer.getFullName(),
                        base.plusDays(1),
                        OrderStatus.DELIVERED,
                        ORDER_DEMO_CHECKOUT_BATCH,
                        List.of(lineSpec(sellable.get(2), 3))
                );
                orderRepository.save(order2);
                applyCommittedInventory(order2);
            }

            log.info("Seeded demo orders (with order_items, product_stock quantities, stock_reservations COMMITTED)");
        } catch (DataAccessException ex) {
            log.warn("Skip order demo seed (data access): {}", ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            log.warn("Skip order demo seed: {}", ex.getMessage(), ex);
        }
    }

    private record SellablePick(long postgresShopId, List<ProductEntity> products) {
    }

    /**
     * Ưu tiên shop Mongo {@code "1"}; nếu không đủ 2 SKU/LEGACY bán được thì chọn shop đầu tiên (shopId số)
     * có ≥ 2 sản phẩm và tồn tại trong Postgres {@code shops}.
     */
    private SellablePick resolveSellablePick() {
        List<ProductEntity> forShop1 = loadSellableForShopKey(DEMO_SHOP_ID_STRING);
        log.info(
                "OrderDemoDataSeeder: sellable for Mongo shopId '{}' = {} (need >= 2)",
                DEMO_SHOP_ID_STRING,
                forShop1.size()
        );
        if (forShop1.size() >= 2) {
            return new SellablePick(DEMO_SHOP_PK, forShop1.stream().limit(4).toList());
        }

        Map<Long, List<ProductEntity>> byShopPk = productRepository.findAll().stream()
                .filter(OrderDemoDataSeeder::isSellableProduct)
                .filter(p -> parseShopPkOrNull(p) != null)
                .collect(Collectors.groupingBy(
                        OrderDemoDataSeeder::parseShopPkOrNull,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return byShopPk.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .filter(e -> shopRepository.existsById(e.getKey()))
                .min(Comparator.comparingLong(Map.Entry::getKey))
                .map(e -> new SellablePick(e.getKey(), e.getValue().stream().limit(4).toList()))
                .orElse(new SellablePick(DEMO_SHOP_PK, List.of()));
    }

    private List<ProductEntity> loadSellableForShopKey(String shopKey) {
        try {
            long pk = Long.parseLong(shopKey.trim());
            List<ProductEntity> flexible = findSellableMongoByShopNumericId(pk, 200);
            if (flexible.size() >= 2 || !DEMO_SHOP_ID_STRING.equals(shopKey)) {
                return flexible.stream().limit(4).toList();
            }
            List<ProductEntity> scanned = productRepository.findAll().stream()
                    .filter(OrderDemoDataSeeder::isSellableProduct)
                    .filter(p -> Long.valueOf(pk).equals(parseShopPkOrNull(p)))
                    .limit(4)
                    .toList();
            return !scanned.isEmpty() ? scanned : flexible.stream().limit(4).toList();
        } catch (NumberFormatException ex) {
            return productRepository.findByShopId(shopKey, PageRequest.of(0, 200)).getContent().stream()
                    .filter(OrderDemoDataSeeder::isSellableProduct)
                    .limit(4)
                    .toList();
        }
    }

    private long countSellableForShopIdString(String shopKey) {
        try {
            long pk = Long.parseLong(shopKey.trim());
            long n = findSellableMongoByShopNumericId(pk, 500).size();
            if (n >= 2 || !DEMO_SHOP_ID_STRING.equals(shopKey)) {
                return n;
            }
            return productRepository.findAll().stream()
                    .filter(OrderDemoDataSeeder::isSellableProduct)
                    .filter(p -> Long.valueOf(pk).equals(parseShopPkOrNull(p)))
                    .count();
        } catch (NumberFormatException ex) {
            return productRepository.findByShopId(shopKey, PageRequest.of(0, 500)).getContent().stream()
                    .filter(OrderDemoDataSeeder::isSellableProduct)
                    .count();
        }
    }

    /**
     * Khớp {@code shopId}/{@code shop_id} dạng chuỗi hoặc số (Mongo thường lưu Int32), loại PARENT, status ACTIVE hoặc thiếu.
     */
    private List<ProductEntity> findSellableMongoByShopNumericId(long shopPk, int limit) {
        int asInt = (int) shopPk;
        Criteria shop = new Criteria().orOperator(
                Criteria.where("shopId").in(String.valueOf(shopPk), shopPk, asInt),
                Criteria.where("shop_id").in(String.valueOf(shopPk), shopPk, asInt)
        );
        Criteria notParent = Criteria.where("product_kind").nin(ProductKind.PARENT.name());
        Criteria statusOk = new Criteria().orOperator(
                Criteria.where("status").exists(false),
                Criteria.where("status").is(null),
                Criteria.where("status").regex("^ACTIVE$", "i")
        );
        Query q = new Query(new Criteria().andOperator(shop, notParent, statusOk));
        q.limit(limit);
        return mongoTemplate.find(q, ProductEntity.class);
    }

    private static boolean isSellableProduct(ProductEntity p) {
        if (ProductKind.resolve(p) == ProductKind.PARENT) {
            return false;
        }
        return isActiveOrUnsetStatus(p);
    }

    /** Cho seed dev: status null/blank coi như đang bán (dữ liệu Mongo cũ). */
    private static boolean isActiveOrUnsetStatus(ProductEntity p) {
        String st = p.getStatus();
        if (st == null || st.isBlank()) {
            return true;
        }
        return "ACTIVE".equalsIgnoreCase(st.trim());
    }

    private static Long parseShopPkOrNull(ProductEntity p) {
        String raw = normalizeShopId(p);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim()).longValue();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String normalizeShopId(ProductEntity p) {
        if (p.getShopId() == null) {
            return null;
        }
        return p.getShopId().trim();
    }

    private record LineSpec(String productId, String productName, BigDecimal unitPrice, int quantity) {
    }

    private static LineSpec lineSpec(ProductEntity p, int qty) {
        BigDecimal unit = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
        return new LineSpec(p.getId(), p.getName() != null ? p.getName() : p.getId(), unit, qty);
    }

    private static OrderEntity buildOrder(
            long shopId,
            long customerId,
            String customerName,
            LocalDateTime createdAt,
            OrderStatus status,
            UUID checkoutBatchId,
            List<LineSpec> lines
    ) {
        List<OrderItemEntity> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        OrderEntity order = OrderEntity.builder()
                .shopId(shopId)
                .customerId(customerId)
                .customerName(customerName != null && !customerName.isBlank() ? customerName : "Customer")
                .status(status)
                .paymentMethod("COD")
                .shippingAddress("234 Võ Văn Tần, Phường 6, Quận 3, TP. Hồ Chí Minh")
                .checkoutBatchId(checkoutBatchId)
                .createdAt(createdAt)
                .items(items)
                .build();

        for (LineSpec spec : lines) {
            BigDecimal subtotal = spec.unitPrice().multiply(BigDecimal.valueOf(spec.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);
            OrderItemEntity line = OrderItemEntity.builder()
                    .order(order)
                    .productId(spec.productId())
                    .productName(spec.productName().length() > 255 ? spec.productName().substring(0, 255) : spec.productName())
                    .unitPrice(spec.unitPrice().setScale(2, RoundingMode.HALF_UP))
                    .quantity(spec.quantity())
                    .subtotal(subtotal)
                    .build();
            items.add(line);
        }
        order.setTotalAmount(total);
        return order;
    }

    private void applyCommittedInventory(OrderEntity saved) {
        Long shopId = saved.getShopId();
        LocalDateTime now = LocalDateTime.now();
        for (OrderItemEntity item : saved.getItems()) {
            stockReservationRepository.save(StockReservationEntity.builder()
                    .orderId(saved.getId())
                    .orderItemId(item.getId())
                    .shopId(shopId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .status(ReservationStatus.COMMITTED)
                    .createdAt(now)
                    .build());

            ProductStockEntity stock = productStockRepository.findByShopIdAndProductId(shopId, item.getProductId())
                    .orElseThrow(() -> new IllegalStateException("product_stock missing for product " + item.getProductId()));
            int next = stock.getQuantityOnHand() - item.getQuantity();
            if (next < 0) {
                throw new IllegalStateException("Negative stock after demo order for product " + item.getProductId());
            }
            stock.setQuantityOnHand(next);
            stock.setUpdatedAt(now);
            productStockRepository.save(stock);
        }
    }

    private void upsertStock(long shopId, String productId, int quantityOnHand) {
        LocalDateTime now = LocalDateTime.now();
        productStockRepository.ensureRowExists(shopId, productId);
        productStockRepository.flush();
        ProductStockEntity row = productStockRepository.findByShopIdAndProductId(shopId, productId)
                .orElseThrow(() -> new IllegalStateException("product_stock missing after ensure for shopId=" + shopId + " productId=" + productId));
        row.setQuantityOnHand(quantityOnHand);
        row.setUpdatedAt(now);
        if (row.getCreatedAt() == null) {
            row.setCreatedAt(now);
        }
        productStockRepository.save(row);
    }
}
