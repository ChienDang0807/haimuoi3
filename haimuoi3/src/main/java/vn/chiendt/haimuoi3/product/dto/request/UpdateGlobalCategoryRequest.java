package vn.chiendt.haimuoi3.product.dto.request;

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
public class UpdateGlobalCategoryRequest {

    private String name;
    private String description;
    private String subtitle;
    private String ctaText;
    private String route;
    private String imageUrl;
    private Integer displayOrder;
}
