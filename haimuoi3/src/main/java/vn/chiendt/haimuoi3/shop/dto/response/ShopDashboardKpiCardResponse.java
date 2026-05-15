package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDashboardKpiCardResponse {
    private String label;
    private String value;
    private String icon;
    private ShopDashboardKpiTrendResponse trend;
}
