package vn.chiendt.haimuoi3.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long shopId;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private String stripeSessionId;
    /** Cùng giá trị cho mọi đơn sinh ra trong một lần checkout đa shop; null nếu đơn đơn lẻ / API cũ. */
    private String checkoutBatchId;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
