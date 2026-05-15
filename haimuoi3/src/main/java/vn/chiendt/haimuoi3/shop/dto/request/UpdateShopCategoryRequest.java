package vn.chiendt.haimuoi3.shop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShopCategoryRequest {
    private String name;
    private String slug;
    private String imageUrl;
    private Integer displayOrder;
    private String globalCategoryId;
    private Boolean isActive;
}
