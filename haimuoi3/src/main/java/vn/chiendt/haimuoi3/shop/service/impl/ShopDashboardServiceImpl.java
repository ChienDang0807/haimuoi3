package vn.chiendt.haimuoi3.shop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.inventory.model.postgres.ProductStockEntity;
import vn.chiendt.haimuoi3.inventory.repository.ProductStockRepository;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderItemEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.shop.dto.response.*;
import vn.chiendt.haimuoi3.shop.mapper.ShopDashboardMapper;
import vn.chiendt.haimuoi3.shop.model.postgres.ShopEntity;
import vn.chiendt.haimuoi3.shop.repository.ShopRepository;
import vn.chiendt.haimuoi3.shop.service.ShopDashboardService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopDashboardServiceImpl implements ShopDashboardService {

    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ShopDashboardMapper shopDashboardMapper;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    @Override
    @Transactional(readOnly = true)
    public ShopDashboardResponse getDashboardForShopOwner(Long ownerUserId, String period) {
        // Validate period - only "7d" is supported in MVP
        if (!"7d".equals(period)) {
            throw new IllegalArgumentException("Invalid period: " + period + ". Only '7d' is supported.");
        }

        // Resolve shop from owner ID
        ShopEntity shop = shopRepository.findByOwnerId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found for this owner"));

        Long shopId = shop.getId();

        // Calculate 7-day window (today.minusDays(6) to today inclusive, Asia/Ho_Chi_Minh timezone)
        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate startDate = today.minusDays(6);

        // FIX: endOfPeriod must be start of TOMORROW to include all of today's orders
        LocalDateTime startOfPeriod = startDate.atStartOfDay();
        LocalDateTime endOfPeriod = today.plusDays(1).atStartOfDay();

        // Calculate previous period for trend comparison (7 days before current period)
        LocalDate previousPeriodStartDate = startDate.minusDays(7);
        LocalDateTime startOfPreviousPeriod = previousPeriodStartDate.atStartOfDay();
        LocalDateTime endOfPreviousPeriod = startOfPeriod; // previous period ends where current starts

        // Get orders in current period (for KPIs and status breakdown)
        List<OrderEntity> ordersInPeriod =
                orderRepository.findByShopIdAndCreatedAtBetween(shopId, startOfPeriod, endOfPeriod);

        // Get orders with items eagerly loaded (for top products calculation)
        List<OrderEntity> ordersWithItems =
                orderRepository.findByShopIdAndCreatedAtBetweenWithItems(shopId, startOfPeriod, endOfPeriod);

        // Get orders in previous period (for trend comparison)
        List<OrderEntity> ordersInPreviousPeriod =
                orderRepository.findByShopIdAndCreatedAtBetween(shopId, startOfPreviousPeriod, endOfPreviousPeriod);

        // Calculate KPIs
        List<ShopDashboardKpiCardResponse> kpiCards = calculateKpiCards(
                shopId, ordersInPeriod, ordersInPreviousPeriod, startOfPeriod);

        // Calculate revenue trend (7 daily points)
        List<ShopDashboardRevenuePointResponse> revenueTrend =
                calculateRevenueTrend(ordersInPeriod, startDate, today);

        // Calculate revenue trend for previous period
        List<ShopDashboardRevenuePointResponse> revenueTrendPrevious =
                calculateRevenueTrend(ordersInPreviousPeriod, previousPeriodStartDate, startDate.minusDays(1));

        // Get recent orders (top 10 by createdAt DESC) using paginated query
        List<ShopDashboardRecentOrderResponse> recentOrders = getRecentOrders(shopId, 10);

        // Get order status breakdown
        List<ShopDashboardOrderStatusSliceResponse> orderStatusBreakdown =
                calculateOrderStatusBreakdown(ordersInPeriod);

        // Calculate fulfillment rate
        Integer fulfillmentRatePercent = calculateFulfillmentRate(ordersInPeriod);

        // Get top sellers (top 3 products by sum(quantity) in period)
        List<ShopDashboardTopProductResponse> topProducts = getTopProducts(ordersWithItems, 3);

        // Get low stock alerts (products with quantityOnHand <= threshold)
        List<ShopDashboardLowStockAlertResponse> lowStockAlerts = getLowStockAlerts(shopId);

        // Build shop health response
        ShopDashboardShopHealthResponse shopHealth = ShopDashboardShopHealthResponse.builder()
                .connectionLabel("ỔN ĐỊNH")
                .lastSyncedAt(LocalDateTime.now(ZONE_ID).toString())
                .build();

        // Build header
        ShopDashboardHeaderResponse header = ShopDashboardHeaderResponse.builder()
                .shopName(shop.getShopName())
                .subtitle("Tổng quan 7 ngày gần nhất")
                .build();

        log.info("Dashboard aggregated for shopId={}, ownerUserId={}, period={}", shopId, ownerUserId, period);

        return ShopDashboardResponse.builder()
                .header(header)
                .kpiCards(kpiCards)
                .revenueTrend(revenueTrend)
                .revenueTrendPrevious(revenueTrendPrevious)
                .recentOrders(recentOrders)
                .orderStatusBreakdown(orderStatusBreakdown)
                .fulfillmentRatePercent(fulfillmentRatePercent)
                .topProducts(topProducts)
                .lowStockAlerts(lowStockAlerts)
                .shopHealth(shopHealth)
                .generatedAt(LocalDateTime.now(ZONE_ID).toString())
                .build();
    }

    private List<ShopDashboardKpiCardResponse> calculateKpiCards(
            Long shopId,
            List<OrderEntity> currentPeriodOrders,
            List<OrderEntity> previousPeriodOrders,
            LocalDateTime startOfPeriod) {

        // KPI 1: Revenue (PAID+DELIVERED orders in period)
        BigDecimal currentRevenue = sumRevenue(currentPeriodOrders);
        BigDecimal previousRevenue = sumRevenue(previousPeriodOrders);

        // KPI 2: Order count
        int currentOrderCount = currentPeriodOrders.size();
        int previousOrderCount = previousPeriodOrders.size();

        // KPI 3: New customer count (customers whose FIRST ORDER EVER in this shop falls within current period)
        int currentNewCustomerCount = calculateNewCustomerCount(shopId, currentPeriodOrders, startOfPeriod);
        int previousNewCustomerCount = calculateNewCustomerCount(shopId, previousPeriodOrders,
                startOfPeriod.minusDays(Constants.Dashboard.PERIOD_DAYS));

        // KPI 4: Cancelled order count
        long currentCancelledCount = currentPeriodOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();
        long previousCancelledCount = previousPeriodOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();

        return List.of(
                buildKpiCard("Doanh thu", formatCurrency(currentRevenue), "payments",
                        currentRevenue, previousRevenue, true),
                buildKpiCard("Số đơn", String.valueOf(currentOrderCount), "shopping_cart",
                        BigDecimal.valueOf(currentOrderCount), BigDecimal.valueOf(previousOrderCount), true),
                buildKpiCard("Khách mới", String.valueOf(currentNewCustomerCount), "people",
                        BigDecimal.valueOf(currentNewCustomerCount), BigDecimal.valueOf(previousNewCustomerCount), true),
                buildKpiCard("Tỷ lệ hủy", String.valueOf(currentCancelledCount), "cancel",
                        BigDecimal.valueOf(currentCancelledCount), BigDecimal.valueOf(previousCancelledCount), false)
        );
    }

    private BigDecimal sumRevenue(List<OrderEntity> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.DELIVERED)
                .map(OrderEntity::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Count customers whose first order ever in this shop falls within the given period.
     * Gets unique customer IDs from the period's orders, then checks which ones had NO orders before the period start.
     */
    private int calculateNewCustomerCount(Long shopId, List<OrderEntity> periodOrders, LocalDateTime periodStart) {
        Set<Long> customerIdsInPeriod = periodOrders.stream()
                .map(OrderEntity::getCustomerId)
                .collect(Collectors.toSet());

        if (customerIdsInPeriod.isEmpty()) {
            return 0;
        }

        // Find which of these customers had orders BEFORE the period start
        List<OrderEntity> priorOrders = orderRepository.findByShopIdAndCreatedAtBeforeAndCustomerIdIn(
                shopId, periodStart, customerIdsInPeriod);

        Set<Long> customersWithPriorOrders = priorOrders.stream()
                .map(OrderEntity::getCustomerId)
                .collect(Collectors.toSet());

        // New customers = those in period who had NO prior orders
        return (int) customerIdsInPeriod.stream()
                .filter(id -> !customersWithPriorOrders.contains(id))
                .count();
    }

    private ShopDashboardKpiCardResponse buildKpiCard(String label, String value, String icon,
                                                       BigDecimal current, BigDecimal previous,
                                                       boolean increaseIsPositive) {
        String trendDirection = calculateTrendDirection(current, previous);
        String trendValue = calculateTrendValue(current, previous);
        // For "cancelled" KPI, increase is negative (increaseIsPositive = false)
        boolean isPositive;
        if (increaseIsPositive) {
            isPositive = "up".equals(trendDirection);
        } else {
            isPositive = "down".equals(trendDirection);
        }

        return ShopDashboardKpiCardResponse.builder()
                .label(label)
                .value(value)
                .icon(icon)
                .trend(ShopDashboardKpiTrendResponse.builder()
                        .direction(trendDirection)
                        .value(trendValue)
                        .isPositive(isPositive)
                        .build())
                .build();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "0 ₫";
        }
        // Format as VND: 123456789 -> 123.456.789 ₫
        String formatted = amount.setScale(0, RoundingMode.HALF_UP).toString();
        StringBuilder result = new StringBuilder();
        int length = formatted.length();
        for (int i = 0; i < length; i++) {
            if (i > 0 && (length - i) % 3 == 0) {
                result.append(".");
            }
            result.append(formatted.charAt(i));
        }
        result.append(" ₫");
        return result.toString();
    }

    private String calculateTrendValue(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return "0%";
            }
            return "+100%";
        }
        BigDecimal percent = current.subtract(previous)
                .divide(previous, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        int percentInt = percent.intValue(); // Use intValue() to avoid ArithmeticException
        String sign = percentInt >= 0 ? "+" : "";
        return String.format("%s%d%%", sign, percentInt);
    }

    private String calculateTrendDirection(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "up" : "neutral";
        }
        if (current.compareTo(previous) > 0) {
            return "up";
        } else if (current.compareTo(previous) < 0) {
            return "down";
        }
        return "neutral";
    }

    /**
     * Generate exactly 7 daily revenue points from startDate to endDate (inclusive).
     */
    private List<ShopDashboardRevenuePointResponse> calculateRevenueTrend(
            List<OrderEntity> orders,
            LocalDate startDate,
            LocalDate endDate) {

        // Group orders by day
        Map<LocalDate, List<OrderEntity>> ordersByDay = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        // Generate daily points from startDate to endDate inclusive
        List<ShopDashboardRevenuePointResponse> result = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            List<OrderEntity> dailyOrders = ordersByDay.getOrDefault(currentDate, Collections.emptyList());

            BigDecimal dailyRevenue = dailyOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.DELIVERED)
                    .map(OrderEntity::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(ShopDashboardRevenuePointResponse.builder()
                    .label(currentDate.format(DAY_LABEL_FORMAT))
                    .revenue(dailyRevenue.longValue())
                    .orderCount(dailyOrders.size())
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    /**
     * Get recent orders using paginated query (top 10 by createdAt DESC).
     */
    private List<ShopDashboardRecentOrderResponse> getRecentOrders(Long shopId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<OrderEntity> orders = orderRepository.findByShopId(shopId, pageRequest).getContent();

        return orders.stream()
                .map(shopDashboardMapper::toRecentOrderResponse)
                .collect(Collectors.toList());
    }

    private List<ShopDashboardOrderStatusSliceResponse> calculateOrderStatusBreakdown(
            List<OrderEntity> orders) {

        Map<OrderStatus, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getStatus, Collectors.counting()));

        List<ShopDashboardOrderStatusSliceResponse> result = new ArrayList<>();

        for (Map.Entry<OrderStatus, Long> entry : statusCounts.entrySet()) {
            OrderStatus status = entry.getKey();
            long count = entry.getValue();

            result.add(ShopDashboardOrderStatusSliceResponse.builder()
                    .statusKey(status.name())
                    .label(getStatusLabel(status))
                    .count((int) count)
                    .colorToken(getStatusColorToken(status))
                    .build());
        }

        return result;
    }

    private Integer calculateFulfillmentRate(List<OrderEntity> orders) {
        if (orders.isEmpty()) {
            return 0;
        }

        long fulfilledCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.SHIPPING ||
                        o.getStatus() == OrderStatus.DELIVERED)
                .count();

        return (int) ((fulfilledCount * 100L) / orders.size());
    }

    /**
     * Get top products by sum(quantity) from eagerly-loaded orders.
     * Resolves product names and images from MongoDB ProductRepository.
     */
    private List<ShopDashboardTopProductResponse> getTopProducts(List<OrderEntity> ordersWithItems, int limit) {
        // Calculate product sales from order items
        Map<String, Integer> productSales = new HashMap<>();
        for (OrderEntity order : ordersWithItems) {
            if (order.getItems() != null) {
                for (OrderItemEntity item : order.getItems()) {
                    productSales.merge(item.getProductId(), item.getQuantity(), Integer::sum);
                }
            }
        }

        if (productSales.isEmpty()) {
            return Collections.emptyList();
        }

        // Get top products by sales count
        List<Map.Entry<String, Integer>> topEntries = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // Fetch product details from MongoDB
        List<String> productIds = topEntries.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Map<String, ProductEntity> productMap = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p, (a, b) -> a));

        return topEntries.stream()
                .map(entry -> {
                    String productId = entry.getKey();
                    Integer salesCount = entry.getValue();
                    ProductEntity product = productMap.get(productId);

                    String name = product != null ? product.getName() : "Sản phẩm #" + productId;
                    String imageUrl = null;
                    if (product != null && product.getProductPictures() != null && !product.getProductPictures().isEmpty()) {
                        imageUrl = product.getProductPictures().get(0).getUrl();
                    }

                    return ShopDashboardTopProductResponse.builder()
                            .productId(productId)
                            .name(name)
                            .imageUrl(imageUrl)
                            .salesCount(salesCount)
                            .trendPercent(0)
                            .trendDirection("neutral")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get low stock alerts: products with quantityOnHand <= LOW_STOCK_THRESHOLD scoped to the shop.
     * Resolves product names from MongoDB.
     */
    private List<ShopDashboardLowStockAlertResponse> getLowStockAlerts(Long shopId) {
        List<ProductStockEntity> lowStockItems = productStockRepository
                .findByShopIdAndQuantityOnHandLessThanEqual(shopId, Constants.Dashboard.LOW_STOCK_THRESHOLD);

        if (lowStockItems.isEmpty()) {
            return Collections.emptyList();
        }

        // Resolve product names from MongoDB
        List<String> productIds = lowStockItems.stream()
                .map(ProductStockEntity::getProductId)
                .collect(Collectors.toList());

        Map<String, String> productNameMap = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, ProductEntity::getName, (a, b) -> a));

        return lowStockItems.stream()
                .map(stock -> ShopDashboardLowStockAlertResponse.builder()
                        .productId(stock.getProductId())
                        .productName(productNameMap.getOrDefault(stock.getProductId(), "Sản phẩm #" + stock.getProductId()))
                        .quantityOnHand(stock.getQuantityOnHand())
                        .build())
                .collect(Collectors.toList());
    }

    private String getStatusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Đang chờ";
            case PENDING_PAYMENT -> "Chờ thanh toán";
            case CONFIRMED -> "Đã xác nhận";
            case PAID -> "Đã thanh toán";
            case READY_TO_SHIP -> "Sẵn sàng giao";
            case SHIPPING -> "Đang giao";
            case DELIVERED -> "Đã giao";
            case CANCELLED -> "Đã hủy";
            case PAYMENT_FAILED -> "Thanh toán thất bại";
        };
    }

    private String getStatusColorToken(OrderStatus status) {
        return switch (status) {
            case DELIVERED -> "success";
            case SHIPPING -> "primary";
            case CONFIRMED, PAID, READY_TO_SHIP -> "warning";
            case CANCELLED -> "danger";
            default -> "info";
        };
    }
}
