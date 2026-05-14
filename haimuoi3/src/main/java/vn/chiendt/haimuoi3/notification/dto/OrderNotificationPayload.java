package vn.chiendt.haimuoi3.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.chiendt.haimuoi3.notification.model.NotificationPayload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationPayload implements NotificationPayload {
    private Long orderId;
    private Long shopId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
