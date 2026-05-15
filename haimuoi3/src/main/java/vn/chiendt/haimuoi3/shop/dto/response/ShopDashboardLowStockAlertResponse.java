package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDashboardLowStockAlertResponse {
    private String productId;
    private String productName;
    private Integer quantityOnHand;
}
