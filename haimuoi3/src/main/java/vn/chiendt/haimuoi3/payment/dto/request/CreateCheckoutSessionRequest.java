package vn.chiendt.haimuoi3.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutSessionRequest {
    private Long orderId;
}
