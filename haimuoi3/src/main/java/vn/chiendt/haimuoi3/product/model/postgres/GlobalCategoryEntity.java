package vn.chiendt.haimuoi3.product.model.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "global_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "global_category_id")
    private String globalCategoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private boolean isActive = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_data", columnDefinition = "jsonb")
    private Map<String, Object> metaData;
}
