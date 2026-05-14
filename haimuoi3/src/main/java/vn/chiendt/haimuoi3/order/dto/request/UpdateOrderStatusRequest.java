package vn.chiendt.haimuoi3.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}
