package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDashboardTopProductResponse {
    private String productId;
    private String name;
    private String imageUrl;
    private Integer salesCount;
    private Integer trendPercent;
    private String trendDirection;
}
