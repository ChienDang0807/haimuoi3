package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopDashboardOrderStatusSliceResponse {
    private String statusKey;
    private String label;
    private Integer count;
    private String colorToken;
}
