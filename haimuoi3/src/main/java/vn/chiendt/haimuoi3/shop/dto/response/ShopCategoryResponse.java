package vn.chiendt.haimuoi3.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopCategoryResponse {
    private String shopCategoryId;
    private String shopId;
    private String globalCategoryId;
    private String name;
    private String slug;
    private String imageUrl;
    private Integer displayOrder;
    private boolean active;
    /** Tên global (chỉ điền khi có globalCategoryId) */
    private String globalCategoryName;
}
