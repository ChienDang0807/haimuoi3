package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponse {
    private Long id;
    private Long ownerId;
    private String shopName;
    private String slug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String email;
    private String phone;
    private String province;
    private String district;
    private String addressDetail;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
