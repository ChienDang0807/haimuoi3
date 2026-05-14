package vn.chiendt.haimuoi3.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private Long shopId;
    private String customerName;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String shippingAddress;
    private List<OrderItemRequest> items;
}
