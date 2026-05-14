package vn.chiendt.haimuoi3.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutBatchResponse {

    private String checkoutBatchId;

    private List<OrderResponse> orders;
}
