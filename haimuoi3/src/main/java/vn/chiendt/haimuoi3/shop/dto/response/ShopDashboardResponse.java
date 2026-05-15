package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDashboardResponse {
    private ShopDashboardHeaderResponse header;
    private List<ShopDashboardKpiCardResponse> kpiCards;
    private List<ShopDashboardRevenuePointResponse> revenueTrend;
    private List<ShopDashboardRevenuePointResponse> revenueTrendPrevious;
    private List<ShopDashboardRecentOrderResponse> recentOrders;
    private List<ShopDashboardOrderStatusSliceResponse> orderStatusBreakdown;
    private Integer fulfillmentRatePercent;
    private List<ShopDashboardTopProductResponse> topProducts;
    private List<ShopDashboardLowStockAlertResponse> lowStockAlerts;
    private ShopDashboardShopHealthResponse shopHealth;
    private String generatedAt;
}
