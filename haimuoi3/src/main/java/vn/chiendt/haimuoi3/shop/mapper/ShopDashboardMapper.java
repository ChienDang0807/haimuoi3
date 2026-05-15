package vn.chiendt.haimuoi3.shop.mapper;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.shop.dto.response.ShopDashboardRecentOrderResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class ShopDashboardMapper {

    public ShopDashboardRecentOrderResponse toRecentOrderResponse(OrderEntity entity) {
        String customerName = "Khách hàng"; // Default - in real implementation, would fetch from user service
        String customerInitials = "KH";
        String customerColor = "#4CAF50";

        // Generate initials from customer name if available
        if (customerName != null && !customerName.isEmpty()) {
            String[] parts = customerName.split(" ");
            if (parts.length >= 2) {
                customerInitials = (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
            } else {
                customerInitials = customerName.substring(0, Math.min(2, customerName.length())).toUpperCase();
            }
            // Generate color from name hash
            int hash = customerName.hashCode();
            customerColor = String.format("#%06X", (0xFFFFFF & (hash * 123456)));
        }

        String statusLabel = getStatusLabel(entity.getStatus());
        String timeAgoLabel = getTimeAgoLabel(entity.getCreatedAt());

        return ShopDashboardRecentOrderResponse.builder()
                .orderId(entity.getId())
                .displayOrderCode("#" + entity.getId())
                .customerName(customerName)
                .customerInitials(customerInitials)
                .customerColor(customerColor)
                .itemCount(entity.getItems() != null ? entity.getItems().size() : 0)
                .status(entity.getStatus().name())
                .statusLabel(statusLabel)
                .timeAgoLabel(timeAgoLabel)
                .totalAmount(entity.getTotalAmount() != null ? entity.getTotalAmount().longValue() : 0L)
                .build();
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
            default -> status.name();
        };
    }

    private String getTimeAgoLabel(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + " giờ trước";
        } else {
            long days = minutes / 1440;
            return days + " ngày trước";
        }
    }
}