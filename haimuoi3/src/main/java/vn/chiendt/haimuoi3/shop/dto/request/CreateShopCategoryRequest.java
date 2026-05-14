package vn.chiendt.haimuoi3.shop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShopCategoryRequest {
    private String name;
    private String slug;
    private Integer displayOrder;
    private String imageUrl;
    private Boolean isActive;
    /** Optional — map tới global_categories.global_category_id */
    private String globalCategoryId;
}
