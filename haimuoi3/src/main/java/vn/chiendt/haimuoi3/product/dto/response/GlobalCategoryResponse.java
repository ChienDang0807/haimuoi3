package vn.chiendt.haimuoi3.product.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalCategoryResponse {
    private String globalCategoryId;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private boolean isActive;
    private String subtitle;
    private String ctaText;
    private String route;
    private Map<String, Object> metaData;
}
