package vn.chiendt.haimuoi3.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminModerationProductResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private String status;
    private String shopId;
    private String imageUrl;
}
