package vn.chiendt.haimuoi3.product.dto.request;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGlobalCategoryRequest {
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    @Builder.Default
    private boolean isActive = true;
    private String subtitle;
    private String ctaText;
    private String route;
    private Map<String, Object> metaData;
}
