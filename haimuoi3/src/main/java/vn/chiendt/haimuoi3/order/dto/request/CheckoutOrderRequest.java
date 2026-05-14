package vn.chiendt.haimuoi3.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutOrderRequest {

    /** Mongo id của giỏ `ACTIVE` của khách; phải khớp giỏ hiện tại. */
    private String cartId;

    private String customerName;

    private String shippingAddress;

    private String paymentMethod;
}
