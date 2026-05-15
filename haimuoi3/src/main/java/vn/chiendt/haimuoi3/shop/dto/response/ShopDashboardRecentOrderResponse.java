package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDashboardRecentOrderResponse {
    private Long orderId;
    private String displayOrderCode;
    private String customerName;
    private String customerInitials;
    private String customerColor;
    private Integer itemCount;
    private String status;
    private String statusLabel;
    private String timeAgoLabel;
    private Long totalAmount;
}
