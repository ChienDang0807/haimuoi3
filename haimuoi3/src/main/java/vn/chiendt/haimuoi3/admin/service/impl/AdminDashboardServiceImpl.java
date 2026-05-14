package vn.chiendt.haimuoi3.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.admin.dto.response.AdminDashboardMetricsResponse;
import vn.chiendt.haimuoi3.admin.dto.response.AdminMetricResponse;
import vn.chiendt.haimuoi3.admin.dto.response.AdminModerationProductResponse;
import vn.chiendt.haimuoi3.admin.dto.response.AdminTrendPointResponse;
import vn.chiendt.haimuoi3.admin.service.AdminDashboardService;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.product.mapper.ProductMapper;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.review.repository.ProductReviewRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int MAX_RANGE_DAYS = 90;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardMetricsResponse getMetrics(LocalDate from, LocalDate to, String bucket) {
        LocalDate resolvedTo = to != null ? to : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(6);
        validateDateRange(resolvedFrom, resolvedTo);

        LocalDateTime fromDateTime = resolvedFrom.atStartOfDay();
        LocalDateTime toDateTime = resolvedTo.plusDays(1).atStartOfDay();
        List<OrderEntity> orders = orderRepository.findByCreatedAtBetween(fromDateTime, toDateTime);

        BigDecimal revenue = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.DELIVERED)
                .map(OrderEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeOrders = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.CANCELLED)
                .count();

        return AdminDashboardMetricsResponse.builder()
                .metrics(List.of(
                        metric("Total Revenue", "$" + revenue, "insights", "up", "current range"),
                        metric("Active Orders", String.valueOf(activeOrders), "shopping_cart", "neutral", "current range"),
                        metric("Products", String.valueOf(productRepository.count()), "inventory", "neutral", "all time"),
                        metric("Reviews", String.valueOf(productReviewRepository.count()), "rate_review", "neutral", "all time")
                ))
                .revenueTrend(buildDailyTrend(orders, resolvedFrom, resolvedTo))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminModerationProductResponse> searchModerationProducts(String query, String status, Pageable pageable) {
        return productRepository.findProductsForModeration(query, status, pageable)
                .map(this::toModerationProduct);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new IllegalArgumentException("date range must be less than or equal to 90 days");
        }
    }

    private List<AdminTrendPointResponse> buildDailyTrend(List<OrderEntity> orders, LocalDate from, LocalDate to) {
        Map<LocalDate, List<OrderEntity>> byDate = orders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate()));

        List<AdminTrendPointResponse> result = new ArrayList<>();
        for (LocalDate cursor = from; !cursor.isAfter(to); cursor = cursor.plusDays(1)) {
            List<OrderEntity> dayOrders = byDate.getOrDefault(cursor, List.of());
            BigDecimal revenue = dayOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.DELIVERED)
                    .map(OrderEntity::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(AdminTrendPointResponse.builder()
                    .label(cursor.toString())
                    .revenue(revenue)
                    .orderCount(dayOrders.size())
                    .build());
        }
        return result.stream()
                .sorted(Comparator.comparing(AdminTrendPointResponse::getLabel))
                .toList();
    }

    private AdminMetricResponse metric(String label, String value, String icon, String direction, String trend) {
        return AdminMetricResponse.builder()
                .label(label)
                .value(value)
                .icon(icon)
                .trendDirection(direction)
                .trendValue(trend)
                .build();
    }

    private AdminModerationProductResponse toModerationProduct(ProductEntity entity) {
        return AdminModerationProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .shopId(entity.getShopId())
                .imageUrl(productMapper.extractImageUrl(entity.getProductPictures()))
                .build();
    }
}
