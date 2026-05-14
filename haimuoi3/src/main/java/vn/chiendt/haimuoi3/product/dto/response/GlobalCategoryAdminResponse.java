package vn.chiendt.haimuoi3.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalCategoryAdminResponse {

    private String globalCategoryId;
    private String name;
    private String slug;
    private String description;
    private String subtitle;
    private String ctaText;
    private String route;
    private String imageUrl;
    private Integer displayOrder;
    private boolean isActive;
    private long productCount;
    private Map<String, Object> metaData;
}
